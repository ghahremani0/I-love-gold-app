package gold.app.ghahremani.ui.screens.gold

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gold.app.ghahremani.data.dao.CalculationDao
import gold.app.ghahremani.data.entity.CalculationEntity
import gold.app.ghahremani.util.GoldPriceFetcher
import gold.app.ghahremani.util.JalaliCalendar
import gold.app.ghahremani.util.NumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

/**
 * حالت‌های مختلف محاسبه طلا
 */
enum class GoldCalcMode { FORWARD, REVERSE }

enum class WeightUnit(val displayName: String, val grams: BigDecimal) {
    GRAM("گرم", BigDecimal("1")),
    MESGHAL("مثقال", BigDecimal("4.6083")),
    SOT("سوت", BigDecimal("0.001")),
    GERAT("قیراط", BigDecimal("0.2"))
}

enum class Karat(val displayName: String, val value: Int) {
    K18("۱۸ عیار", 18),
    K20("۲۰ عیار", 20),
    K21("۲۱ عیار", 21),
    K22("۲۲ عیار", 22),
    K24("۲۴ عیار", 24),
    MELTED("آب‌شده", 700)
}
enum class MakingChargeType { PERCENT, FIXED }

/**
 * قیمت‌های تابلو برای نمایش
 */
data class DashboardPrices(
    val gold18: String = "",
    val gold24: String = "",
    val mesghal: String = "",
    val ounce: String = "",
    val lastUpdate: String = ""
)

/**
 * نتیجه محاسبه طلا
 */
data class GoldResult(
    val basePrice: BigDecimal = BigDecimal.ZERO,
    val makingCharge: BigDecimal = BigDecimal.ZERO,
    val profit: BigDecimal = BigDecimal.ZERO,
    val tax: BigDecimal = BigDecimal.ZERO,
    val finalPrice: BigDecimal = BigDecimal.ZERO,
    val weight: BigDecimal = BigDecimal.ZERO
)

/**
 * وضعیت صفحه ماشین‌حساب طلا
 */
data class GoldCalculatorState(
    val goldPrice: String = "",
    val weight: String = "",
    val selectedUnit: WeightUnit = WeightUnit.GRAM,
    val selectedKarat: Karat = Karat.K18,
    val makingChargeType: MakingChargeType = MakingChargeType.PERCENT,
    val makingChargeValue: String = "",
    val sellerProfitPercent: String = "",
    val taxPercent: String = "9",
    val calcMode: GoldCalcMode = GoldCalcMode.FORWARD,
    val reverseFinalPrice: String = "",
    val result: GoldResult = GoldResult(),
    val isFetchingPrice: Boolean = false,
    val fetchMessage: String = "",
    val isSaved: Boolean = false,
    val dashboard: DashboardPrices = DashboardPrices(),
    val hasDashboard: Boolean = false
)

/**
 * ViewModel صفحه ماشین‌حساب طلا
 */
class GoldCalculatorViewModel(
    private val dao: CalculationDao
) : ViewModel() {

    private val _state = MutableStateFlow(GoldCalculatorState())
    val state: StateFlow<GoldCalculatorState> = _state.asStateFlow()

    private var cachedPrices: GoldPriceFetcher.GoldPrices? = null
    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    /**
     * قیمت فعلی طلای ۱۸ عیار (برای اشتراک با ProViewModel)
     */
    val currentGold18Price: BigDecimal?
        get() = cachedPrices?.gold18PerGram

    fun updateGoldPrice(value: String) {
        _state.value = _state.value.copy(goldPrice = value, isSaved = false)
        calculate()
    }

    fun updateWeight(value: String) {
        _state.value = _state.value.copy(weight = value, isSaved = false)
        calculate()
    }

    fun updateUnit(unit: WeightUnit) {
        _state.value = _state.value.copy(selectedUnit = unit, isSaved = false)
        calculate()
    }

    fun updateKarat(karat: Karat) {
        _state.value = _state.value.copy(selectedKarat = karat, isSaved = false)
        calculate()
    }

    fun updateMakingChargeType(type: MakingChargeType) {
        _state.value = _state.value.copy(makingChargeType = type, isSaved = false)
        calculate()
    }

    fun updateMakingChargeValue(value: String) {
        _state.value = _state.value.copy(makingChargeValue = value, isSaved = false)
        calculate()
    }

    fun updateSellerProfit(value: String) {
        _state.value = _state.value.copy(sellerProfitPercent = value, isSaved = false)
        calculate()
    }

    fun updateTaxPercent(value: String) {
        _state.value = _state.value.copy(taxPercent = value, isSaved = false)
        calculate()
    }

    fun updateReversePrice(value: String) {
        _state.value = _state.value.copy(reverseFinalPrice = value, isSaved = false)
        calculateReverse()
    }

    fun toggleCalcMode() {
        val newMode = if (_state.value.calcMode == GoldCalcMode.FORWARD) GoldCalcMode.REVERSE else GoldCalcMode.FORWARD
        _state.value = _state.value.copy(calcMode = newMode, isSaved = false)
        if (newMode == GoldCalcMode.REVERSE) calculateReverse() else calculate()
    }

    /**
     * دریافت قیمت از تابلو (کش) و قرار دادن در فیلد ورودی قیمت
     * این تابع فقط از آخرین داده‌های تابلو استفاده می‌کند، نه fetch جدید
     */
    fun fetchGoldPrice() {
        val cached = cachedPrices
        if (cached != null) {
            // قیمت از تابلو (کش) دریافت و در فیلد ورودی قرار داده شود
            updatePriceForKarat(_state.value.selectedKarat, cached)
            _state.value = _state.value.copy(
                isFetchingPrice = false,
                fetchMessage = "قیمت از تابلو دریافت شد"
            )
            calculate()
        } else {
            // تابلو هنوز پر نشده است
            _state.value = _state.value.copy(
                isFetchingPrice = false,
                fetchMessage = "تابلو هنوز آماده نیست، چند لحظه صبر کنید..."
            )
        }
    }

    /**
     * به‌روزرسانی خودکار تابلو هر ۳۰ ثانیه
     * فقط تابلو را به‌روز می‌کند، نه فیلد ورودی قیمت را
     */
    private suspend fun refreshDashboardOnly() {
        when (val result = GoldPriceFetcher.fetchGoldPrice()) {
            is GoldPriceFetcher.PriceResult.Success -> {
                cachedPrices = result.prices
                val dash = buildDashboard(result.prices)
                // فقط تابلو به‌روز می‌شود، goldPrice و fetchMessage تغییر نمی‌کنند
                _state.value = _state.value.copy(
                    dashboard = dash,
                    hasDashboard = true
                )
            }
            is GoldPriceFetcher.PriceResult.Error -> {
                // در به‌روزرسانی خودکار خطا را نشان نمی‌دهیم
            }
        }
    }

    /**
     * ساخت داشبورد از قیمت‌های دریافت‌شده با تاریخ شمسی
     */
    private fun buildDashboard(prices: GoldPriceFetcher.GoldPrices): DashboardPrices {
        val now = Calendar.getInstance()
        val jalali = JalaliCalendar.gregorianToJalali(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )
        val timeStr = JalaliCalendar.toPersianDigits(
            String.format("%02d:%02d:%02d", 
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND))
        ) + " - " + jalali.toShortString()
        return DashboardPrices(
            gold18 = NumberFormatter.formatToman(prices.gold18PerGram),
            gold24 = NumberFormatter.formatToman(prices.gold24PerGram),
            mesghal = NumberFormatter.formatToman(prices.mesghal),
            ounce = NumberFormatter.formatToman(prices.ounce),
            lastUpdate = timeStr
        )
    }

    /**
     * شروع به‌روزرسانی خودکار: ابتدا یک fetch فوری، سپس هر ۳۰ ثانیه
     * فقط تابلو را به‌روز می‌کند، نه فیلد ورودی قیمت را
     */
    fun startAutoRefresh() {
        // جلوگیری از اجرای چندگانه
        if (autoRefreshJob?.isActive == true) return
        autoRefreshJob = viewModelScope.launch {
            // اولین دریافت فوری برای پر کردن تابلو
            refreshDashboardOnly()
            // سپس هر ۳۰ ثانیه
            while (true) {
                kotlinx.coroutines.delay(30_000)
                refreshDashboardOnly()
            }
        }
    }

    /**
     * تنظیم قیمت بر اساس عیار انتخاب‌شده
     */
    private fun updatePriceForKarat(karat: Karat, prices: GoldPriceFetcher.GoldPrices) {
        val pricePerGram = when (karat) {
            Karat.K18 -> prices.gold18PerGram
            Karat.K24 -> prices.gold24PerGram
            Karat.MELTED -> prices.gold18PerGram.multiply(BigDecimal("700")).divide(BigDecimal("750"), 2, RoundingMode.HALF_UP)
            else -> {
                // برای عیارهای ۲۰، ۲۱، ۲۲: محاسبه نسبتی از طلای ۲۴
                prices.gold24PerGram.multiply(BigDecimal(karat.value))
                    .divide(BigDecimal("24"), 2, RoundingMode.HALF_UP)
            }
        }
        val formatted = NumberFormatter.formatInput(pricePerGram.toPlainString())
        _state.value = _state.value.copy(goldPrice = formatted)
    }

    /**
     * محاسبه مستقیم: از وزن به قیمت نهایی
     */
    private fun calculate() {
        val s = _state.value
        val goldPrice = NumberFormatter.parseInput(s.goldPrice)
        val weightInput = NumberFormatter.parseInput(s.weight)

        if (goldPrice.compareTo(BigDecimal.ZERO) == 0) {
            _state.value = _state.value.copy(result = GoldResult())
            return
        }

        // تبدیل وزن به گرم
        val weightInGrams = weightInput.multiply(s.selectedUnit.grams)

        // قیمت پایه = قیمت هر گرم × وزن بر حسب گرم
        // قیمت بر اساس عیار انتخاب‌شده است (از سرور دریافت شده)
        val basePrice = goldPrice.multiply(weightInGrams)

        // اجرت
        val makingChargeValue = NumberFormatter.parseInput(s.makingChargeValue)
        val makingCharge = when (s.makingChargeType) {
            MakingChargeType.PERCENT -> {
                val percent = makingChargeValue.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
                NumberFormatter.roundMoney(basePrice.multiply(percent))
            }
            MakingChargeType.FIXED -> NumberFormatter.roundMoney(makingChargeValue)
        }

        // سود = درصد سود × (قیمت پایه + اجرت)
        val profitPercent = NumberFormatter.parseInput(s.sellerProfitPercent)
        val profit = if (profitPercent.compareTo(BigDecimal.ZERO) > 0) {
            val percent = profitPercent.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
            NumberFormatter.roundMoney((basePrice.add(makingCharge)).multiply(percent))
        } else BigDecimal.ZERO

        // مالیات = ۹٪ × (اجرت + سود)
        val taxPercentVal = NumberFormatter.parseInput(s.taxPercent)
        val tax = if (taxPercentVal.compareTo(BigDecimal.ZERO) > 0) {
            val percent = taxPercentVal.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
            NumberFormatter.roundMoney((makingCharge.add(profit)).multiply(percent))
        } else BigDecimal.ZERO

        // قیمت نهایی = قیمت پایه + اجرت + سود + مالیات
        val finalPrice = NumberFormatter.roundMoney(basePrice.add(makingCharge).add(profit).add(tax))

        _state.value = _state.value.copy(
            result = GoldResult(
                basePrice = NumberFormatter.roundMoney(basePrice),
                makingCharge = makingCharge,
                profit = profit,
                tax = tax,
                finalPrice = finalPrice,
                weight = weightInGrams
            )
        )
    }

    /**
     * محاسبه معکوس: از قیمت نهایی به وزن
     */
    private fun calculateReverse() {
        val s = _state.value
        val goldPrice = NumberFormatter.parseInput(s.goldPrice)
        val finalPriceInput = NumberFormatter.parseInput(s.reverseFinalPrice)

        if (goldPrice.compareTo(BigDecimal.ZERO) == 0 || finalPriceInput.compareTo(BigDecimal.ZERO) == 0) {
            _state.value = _state.value.copy(result = GoldResult())
            return
        }

        val makingChargeValue = NumberFormatter.parseInput(s.makingChargeValue)
        val profitPercent = NumberFormatter.parseInput(s.sellerProfitPercent)
        val taxPercentVal = NumberFormatter.parseInput(s.taxPercent)

        val makingChargePercent = if (s.makingChargeType == MakingChargeType.PERCENT) {
            makingChargeValue.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        val profitPercentVal = profitPercent.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
        val taxPercentVal2 = taxPercentVal.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)

        // ضریب کلی: factor = (1 + mc) × (1 + p) × (1 + t)
        val factor = (BigDecimal.ONE.add(makingChargePercent))
            .multiply(BigDecimal.ONE.add(profitPercentVal))
            .multiply(BigDecimal.ONE.add(taxPercentVal2))

        // تأثیر اجرت ثابت
        val fixedMakingCharge = if (s.makingChargeType == MakingChargeType.FIXED) {
            makingChargeValue.multiply(BigDecimal.ONE.add(profitPercentVal))
                .multiply(BigDecimal.ONE.add(taxPercentVal))
        } else BigDecimal.ZERO

        val adjustedFinal = finalPriceInput.subtract(fixedMakingCharge)

        val unitPrice = goldPrice.multiply(factor)

        if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
            _state.value = _state.value.copy(result = GoldResult())
            return
        }

        val weightInGrams = adjustedFinal.divide(unitPrice, 6, RoundingMode.HALF_UP)

        val basePrice = goldPrice.multiply(weightInGrams)
        val makingCharge = when (s.makingChargeType) {
            MakingChargeType.PERCENT -> NumberFormatter.roundMoney(basePrice.multiply(makingChargePercent))
            MakingChargeType.FIXED -> NumberFormatter.roundMoney(makingChargeValue)
        }
        val profit = NumberFormatter.roundMoney((basePrice.add(makingCharge)).multiply(profitPercentVal))
        val tax = NumberFormatter.roundMoney((makingCharge.add(profit)).multiply(taxPercentVal2))
        val finalPrice = NumberFormatter.roundMoney(basePrice.add(makingCharge).add(profit).add(tax))

        _state.value = _state.value.copy(
            result = GoldResult(
                basePrice = NumberFormatter.roundMoney(basePrice),
                makingCharge = makingCharge,
                profit = profit,
                tax = tax,
                finalPrice = finalPrice,
                weight = weightInGrams
            )
        )
    }

    /**
     * ذخیره محاسبه در تاریخچه
     */
    fun saveCalculation() {
        val s = _state.value
        val r = s.result
        if (r.finalPrice.compareTo(BigDecimal.ZERO) == 0) return

        viewModelScope.launch {
            val detail = buildString {
                append("وزن: ${NumberFormatter.formatDecimal(r.weight, 3)} گرم")
                append(" | عیار: ${s.selectedKarat.displayName}")
                if (s.calcMode == GoldCalcMode.REVERSE) append(" | معکوس")
            }
            dao.insert(
                CalculationEntity(
                    type = "gold",
                    title = "محاسبه طلا",
                    result = "${NumberFormatter.formatToman(r.finalPrice)} تومان",
                    details = detail,
                    timestamp = System.currentTimeMillis()
                )
            )
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
