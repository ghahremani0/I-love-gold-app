package gold.app.ghahremani.ui.screens.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gold.app.ghahremani.data.dao.CalculationDao
import gold.app.ghahremani.data.entity.CalculationEntity
import gold.app.ghahremani.util.NumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * روش محاسبه وام
 */
enum class LoanMethod(val displayName: String) {
    REDUCING_BALANCE("قسط کاهشی (سود کاهشی)"),
    SIMPLE_INTEREST("سود ثابت (ساده)")
}

/**
 * واحد مدت بازپرداخت
 */
enum class DurationUnit(val displayName: String) {
    MONTHS("ماه"),
    YEARS("سال")
}

/**
 * مبنای روز برای محاسبه سود روزشمار
 */
enum class DayBasis(val displayName: String, val daysInYear: Int) {
    DAYS_365("سال ۳۶۵ روزه", 365),
    DAYS_360("سال ۳۶۰ روزه", 360),
    PERSIAN("سال شمسی (۱۲×۳۰)", 360)
}

/**
 * ردیف جدول اقساط
 */
data class InstallmentRow(
    val number: Int,
    val payment: BigDecimal,
    val interest: BigDecimal,
    val principal: BigDecimal,
    val remaining: BigDecimal
)

/**
 * نتیجه محاسبه وام
 */
data class LoanResult(
    val monthlyPayment: BigDecimal = BigDecimal.ZERO,
    val totalInterest: BigDecimal = BigDecimal.ZERO,
    val totalPayment: BigDecimal = BigDecimal.ZERO,
    val installments: List<InstallmentRow> = emptyList()
)

/**
 * نتیجه محاسبه سود روزشمار
 */
data class DailyInterestResult(
    val days: Int = 0,
    val totalInterest: BigDecimal = BigDecimal.ZERO,
    val finalAmount: BigDecimal = BigDecimal.ZERO,
    val endDate: String = ""
)

/**
 * وضعیت صفحه وام
 */
data class LoanCalculatorState(
    // حالت فعلی: وام اقساطی یا سود روزشمار
    val currentTab: Int = 0, // 0 = وام اقساطی, 1 = سود روزشمار

    // ورودی‌های وام اقساطی
    val loanAmount: String = "",
    val annualRate: String = "",
    val duration: String = "",
    val durationUnit: DurationUnit = DurationUnit.MONTHS,
    val loanMethod: LoanMethod = LoanMethod.SIMPLE_INTEREST,

    // نتیجه وام
    val loanResult: LoanResult? = null,

    // ورودی‌های سود روزشمار
    val principal: String = "",
    val dailyRate: String = "",
    val dayBasis: DayBasis = DayBasis.DAYS_365,
    val startDate: String = "",
    val endDate: String = "",
    val daysFromToday: String = "",
    val useDaysFromToday: Boolean = false,

    // نتیجه سود روزشمار
    val dailyResult: DailyInterestResult? = null,

    val isSaved: Boolean = false
)

/**
 * ViewModel صفحه ماشین‌حساب وام و سود
 */
class LoanCalculatorViewModel(
    private val dao: CalculationDao
) : ViewModel() {

    private val _state = MutableStateFlow(LoanCalculatorState())
    val state: StateFlow<LoanCalculatorState> = _state.asStateFlow()

    fun updateTab(tab: Int) {
        _state.value = _state.value.copy(currentTab = tab, isSaved = false)
    }

    fun updateLoanAmount(value: String) {
        _state.value = _state.value.copy(loanAmount = value, isSaved = false)
    }

    fun updateAnnualRate(value: String) {
        _state.value = _state.value.copy(annualRate = value, isSaved = false)
    }

    fun updateDuration(value: String) {
        _state.value = _state.value.copy(duration = value, isSaved = false)
    }

    fun updateDurationUnit(unit: DurationUnit) {
        _state.value = _state.value.copy(durationUnit = unit, isSaved = false)
    }

    fun updateLoanMethod(method: LoanMethod) {
        _state.value = _state.value.copy(loanMethod = method, isSaved = false)
    }

    fun updatePrincipal(value: String) {
        _state.value = _state.value.copy(principal = value, isSaved = false)
    }

    fun updateDailyRate(value: String) {
        _state.value = _state.value.copy(dailyRate = value, isSaved = false)
    }

    fun updateDayBasis(basis: DayBasis) {
        _state.value = _state.value.copy(dayBasis = basis, isSaved = false)
    }

    fun updateStartDate(date: String) {
        _state.value = _state.value.copy(startDate = date, isSaved = false)
    }

    fun updateEndDate(date: String) {
        _state.value = _state.value.copy(endDate = date, isSaved = false)
    }

    fun updateDaysFromToday(value: String) {
        _state.value = _state.value.copy(daysFromToday = value, isSaved = false)
    }

    fun toggleUseDaysFromToday() {
        _state.value = _state.value.copy(useDaysFromToday = !_state.value.useDaysFromToday, isSaved = false)
    }

    /**
     * محاسبه وام اقساطی (با دکمه محاسبه)
     */
    fun calculateLoan() {
        val s = _state.value
        val principal = NumberFormatter.parseInput(s.loanAmount)
        val annualRate = NumberFormatter.parseInput(s.annualRate)
        val durationInput = NumberFormatter.parseInput(s.duration)

        if (principal.compareTo(BigDecimal.ZERO) == 0 || annualRate.compareTo(BigDecimal.ZERO) == 0 && s.loanMethod == LoanMethod.REDUCING_BALANCE) {
            return
        }

        // تبدیل مدت به ماه
        val months = when (s.durationUnit) {
            DurationUnit.MONTHS -> durationInput.toInt()
            DurationUnit.YEARS -> durationInput.multiply(BigDecimal("12")).toInt()
        }

        if (months <= 0) return

        val result = when (s.loanMethod) {
            LoanMethod.REDUCING_BALANCE -> calculateReducingBalance(principal, annualRate, months)
            LoanMethod.SIMPLE_INTEREST -> calculateSimpleInterest(principal, annualRate, months)
        }

        _state.value = _state.value.copy(loanResult = result, isSaved = false)
    }

    /**
     * محاسبه قسط کاهشی (سود کاهشی)
     * فرمول: قسط = P×r×(1+r)^n ÷ ((1+r)^n − 1)
     * r = نرخ ماهانه
     */
    private fun calculateReducingBalance(principal: BigDecimal, annualRate: BigDecimal, months: Int): LoanResult {
        val monthlyRate = annualRate.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
            .divide(BigDecimal("12"), 10, RoundingMode.HALF_UP)

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // بدون سود
            val monthlyPayment = principal.divide(BigDecimal(months), 0, RoundingMode.HALF_UP)
            return LoanResult(
                monthlyPayment = monthlyPayment,
                totalInterest = BigDecimal.ZERO,
                totalPayment = principal,
                installments = (1..months).map { i ->
                    InstallmentRow(
                        number = i,
                        payment = monthlyPayment,
                        interest = BigDecimal.ZERO,
                        principal = monthlyPayment,
                        remaining = principal.subtract(monthlyPayment.multiply(BigDecimal(i)))
                            .max(BigDecimal.ZERO)
                    )
                }
            )
        }

        // (1+r)^n
        val onePlusR = BigDecimal.ONE.add(monthlyRate)
        var onePlusRPowerN = BigDecimal.ONE
        repeat(months) { onePlusRPowerN = onePlusRPowerN.multiply(onePlusR) }

        // قسط ماهانه = P × r × (1+r)^n / ((1+r)^n - 1)
        val numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN)
        val denominator = onePlusRPowerN.subtract(BigDecimal.ONE)

        val monthlyPayment = numerator.divide(denominator, 0, RoundingMode.HALF_UP)

        // جدول اقساط
        var remaining = principal
        val installments = (1..months).map { i ->
            val interest = remaining.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP)
            val principalPart = monthlyPayment.subtract(interest)
            remaining = remaining.subtract(principalPart).max(BigDecimal.ZERO)

            InstallmentRow(
                number = i,
                payment = monthlyPayment,
                interest = interest,
                principal = principalPart,
                remaining = remaining
            )
        }

        val totalPayment = monthlyPayment.multiply(BigDecimal(months))
        val totalInterest = totalPayment.subtract(principal)

        return LoanResult(
            monthlyPayment = monthlyPayment,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            installments = installments
        )
    }

    /**
     * محاسبه سود ثابت (ساده) - روش رایج در ایران
     * کل سود = P × نرخ سالانه × تعداد سال
     * قسط = (اصل + کل سود) ÷ تعداد ماه‌ها
     */
    private fun calculateSimpleInterest(principal: BigDecimal, annualRate: BigDecimal, months: Int): LoanResult {
        val years = BigDecimal(months).divide(BigDecimal("12"), 10, RoundingMode.HALF_UP)

        // کل سود = اصل × نرخ سالانه × تعداد سال
        val totalInterest = principal.multiply(annualRate).divide(BigDecimal("100"), 0, RoundingMode.HALF_UP)
            .multiply(years).setScale(0, RoundingMode.HALF_UP)

        val totalPayment = principal.add(totalInterest)

        // قسط ماهانه = (اصل + کل سود) ÷ تعداد ماه‌ها
        val monthlyPayment = totalPayment.divide(BigDecimal(months), 0, RoundingMode.HALF_UP)

        // در روش سود ثابت، سود هر ماه = کل سود / تعداد ماه
        val monthlyInterest = totalInterest.divide(BigDecimal(months), 0, RoundingMode.HALF_UP)
        val monthlyPrincipal = monthlyPayment.subtract(monthlyInterest)

        var remaining = principal
        val installments = (1..months).map { i ->
            remaining = remaining.subtract(monthlyPrincipal).max(BigDecimal.ZERO)
            InstallmentRow(
                number = i,
                payment = monthlyPayment,
                interest = monthlyInterest,
                principal = monthlyPrincipal,
                remaining = remaining
            )
        }

        return LoanResult(
            monthlyPayment = monthlyPayment,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            installments = installments
        )
    }

    /**
     * محاسبه سود روزشمار (با دکمه محاسبه)
     */
    fun calculateDailyInterest() {
        val s = _state.value
        val principal = NumberFormatter.parseInput(s.principal)
        val rate = NumberFormatter.parseInput(s.dailyRate)
        val basis = s.dayBasis

        if (principal.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(BigDecimal.ZERO) == 0) return

        // محاسبه تعداد روز
        val days = if (s.useDaysFromToday) {
            NumberFormatter.parseInput(s.daysFromToday).toInt()
        } else {
            // محاسبه از تاریخ شمسی
            val start = parseJalaliDate(s.startDate) ?: return
            val end = if (s.endDate.isNotEmpty()) {
                parseJalaliDate(s.endDate) ?: return
            } else {
                gold.app.ghahremani.util.JalaliCalendar.today()
            }
            gold.app.ghahremani.util.JalaliCalendar.daysBetween(start, end)
        }

        if (days <= 0) return

        // سود = (اصل × نرخ درصدی × تعداد روز) ÷ (مبنای روز × ۱۰۰)
        val interest = principal.multiply(rate)
            .multiply(BigDecimal(days))
            .divide(BigDecimal(basis.daysInYear * 100), 0, RoundingMode.HALF_UP)

        val finalAmount = principal.add(interest)

        // محاسبه تاریخ پایان برای نمایش
        val endDateStr = if (s.useDaysFromToday) {
            val today = gold.app.ghahremani.util.JalaliCalendar.today()
            val endDate = gold.app.ghahremani.util.JalaliCalendar.addDays(today, days)
            endDate.toString()
        } else if (s.endDate.isNotEmpty()) {
            val end = parseJalaliDate(s.endDate)
            end?.toString() ?: ""
        } else {
            gold.app.ghahremani.util.JalaliCalendar.today().toString()
        }

        _state.value = _state.value.copy(
            dailyResult = DailyInterestResult(
                days = days,
                totalInterest = interest,
                finalAmount = finalAmount,
                endDate = endDateStr
            ),
            isSaved = false
        )
    }

    /**
     * تبدیل رشته تاریخ شمسی به JalaliDate
     */
    private fun parseJalaliDate(dateStr: String): gold.app.ghahremani.util.JalaliCalendar.JalaliDate? {
        val english = gold.app.ghahremani.util.JalaliCalendar.toEnglishDigits(dateStr)
        val parts = english.split("/")
        if (parts.size != 3) return null
        return try {
            gold.app.ghahremani.util.JalaliCalendar.JalaliDate(
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ذخیره محاسبه در تاریخچه
     */
    fun saveCalculation() {
        val s = _state.value
        viewModelScope.launch {
            if (s.currentTab == 0 && s.loanResult != null) {
                val r = s.loanResult
                dao.insert(
                    CalculationEntity(
                        type = "loan",
                        title = "وام اقساطی",
                        result = "${NumberFormatter.formatToman(r.totalPayment)} تومان",
                        details = "قسط ماهانه: ${NumberFormatter.formatToman(r.monthlyPayment)} | سود: ${NumberFormatter.formatToman(r.totalInterest)}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else if (s.currentTab == 1 && s.dailyResult != null) {
                val r = s.dailyResult
                dao.insert(
                    CalculationEntity(
                        type = "daily_interest",
                        title = "سود روزشمار",
                        result = "${NumberFormatter.formatToman(r.finalAmount)} تومان",
                        details = "مدت: ${r.days} روز | سود: ${NumberFormatter.formatToman(r.totalInterest)}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
