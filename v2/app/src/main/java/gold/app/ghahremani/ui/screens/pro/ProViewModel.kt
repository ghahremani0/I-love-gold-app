package gold.app.ghahremani.ui.screens.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gold.app.ghahremani.data.dao.ProDao
import gold.app.ghahremani.data.entity.GoldAssetEntity
import gold.app.ghahremani.data.entity.LoanReminderEntity
import gold.app.ghahremani.data.entity.PriceAlertEntity
import gold.app.ghahremani.util.GoldChartApi
import gold.app.ghahremani.util.GoldPriceFetcher
import gold.app.ghahremani.util.JalaliCalendar
import gold.app.ghahremani.util.NumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ابزارهای موجود در صفحه پرو
 */
enum class ProTool(val title: String, val icon: String, val description: String) {
    GOLD_GIFT("طلای هدیه", "🎁", "دریافت رایگان طلای هدیه"),
    GOLD_CHART("نمودار قیمت طلا", "📈", "مشاهده روند و تحلیل قیمت طلا"),
    PRICE_ALERT("هشدار قیمت طلا", "🔔", "یادآوری رسیدن قیمت به حد مدنظر"),
    PROFIT_LOSS("سود و زیان دارایی", "💰", "محاسبه سود و زیان دارایی طلای شما"),
    BEST_LOANS("معرفی بهترین وام‌ها", "🏦", "بررسی شرایط بهترین وام‌های بازار"),
    LOAN_REMINDER("یادآور اقساط وام", "⏰", "یادآوری سررسید اقساط وام شما")
}

/**
 * داده‌های وام پیشنهادی
 */
data class LoanInfo(
    val title: String,
    val maxAmount: String,
    val interestRate: String,
    val duration: String,
    val guarantee: String,
    val description: String
)

/**
 * داده‌های نمودار قیمت
 */
data class PricePoint(
    val timestamp: Long,
    val price: BigDecimal
)

/**
 * نتیجه محاسبه سود و زیان
 */
data class ProfitLossResult(
    val totalBuyValue: BigDecimal = BigDecimal.ZERO,
    val totalCurrentValue: BigDecimal = BigDecimal.ZERO,
    val totalProfitLoss: BigDecimal = BigDecimal.ZERO,
    val profitLossPercent: BigDecimal = BigDecimal.ZERO
)

/**
 * وضعیت صفحه پرو
 */
data class ProState(
    val selectedTool: ProTool? = null,
    val alerts: List<PriceAlertEntity> = emptyList(),
    val assets: List<GoldAssetEntity> = emptyList(),
    val reminders: List<LoanReminderEntity> = emptyList(),
    val currentGold18Price: BigDecimal? = null,
    val priceHistory: List<PricePoint> = emptyList(),
    val isPriceTracking: Boolean = false,
    val chartPoints: List<GoldChartApi.ChartPoint> = emptyList(),
    val chartSummary: GoldChartApi.ChartSummary? = null,
    val isChartLoading: Boolean = false,
    val chartError: String = ""
)

/**
 * ViewModel برای قابلیت‌های پرو
 */
class ProViewModel(
    private val proDao: ProDao,
    private val onAlertTriggered: (String, String) -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(ProState())
    val state: StateFlow<ProState> = _state.asStateFlow()

    private var priceTrackingJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            proDao.getAllAlerts().collect { alerts ->
                _state.value = _state.value.copy(alerts = alerts)
            }
        }
        viewModelScope.launch {
            proDao.getAllAssets().collect { assets ->
                _state.value = _state.value.copy(assets = assets)
            }
        }
        viewModelScope.launch {
            proDao.getAllReminders().collect { reminders ->
                _state.value = _state.value.copy(reminders = reminders)
            }
        }
    }

    fun selectTool(tool: ProTool?) {
        _state.value = _state.value.copy(selectedTool = tool)
    }

    // --- Price Tracking for Chart ---
    fun startPriceTracking() {
        if (priceTrackingJob?.isActive == true) return
        _state.value = _state.value.copy(isPriceTracking = true)
        priceTrackingJob = viewModelScope.launch {
            // اولین fetch فوری
            fetchAndRecordPrice()
            // سپس هر ۳۰ ثانیه
            while (true) {
                kotlinx.coroutines.delay(30_000)
                fetchAndRecordPrice()
            }
        }
    }

    fun stopPriceTracking() {
        priceTrackingJob?.cancel()
        _state.value = _state.value.copy(isPriceTracking = false)
    }

    private suspend fun fetchAndRecordPrice() {
        when (val result = GoldPriceFetcher.fetchGoldPrice()) {
            is GoldPriceFetcher.PriceResult.Success -> {
                val price = result.prices.gold18PerGram
                val newHistory = (_state.value.priceHistory + PricePoint(System.currentTimeMillis(), price))
                    .takeLast(60)
                _state.value = _state.value.copy(
                    currentGold18Price = price,
                    priceHistory = newHistory
                )
                // بررسی هشدارها
                checkAlerts(price, "18")
            }
            is GoldPriceFetcher.PriceResult.Error -> {}
        }
    }

    /**
     * به‌روزرسانی قیمت از تابلو (از GoldCalculatorViewModel)
     */
    fun updateCurrentPrice(price: BigDecimal) {
        val newHistory = (_state.value.priceHistory + PricePoint(System.currentTimeMillis(), price))
            .takeLast(60)
        _state.value = _state.value.copy(
            currentGold18Price = price,
            priceHistory = newHistory
        )
        checkAlerts(price, "18")
    }

    // --- Chart from API ---
    fun fetchChartSummary() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isChartLoading = true, chartError = "")
            when (val result = GoldChartApi.fetchChartHistory("7d")) {
                is GoldChartApi.ChartResult.Success -> {
                    _state.value = _state.value.copy(
                        chartPoints = result.data.points,
                        chartSummary = result.data.summary,
                        isChartLoading = false,
                        currentGold18Price = result.data.points.lastOrNull()?.price
                    )
                }
                is GoldChartApi.ChartResult.Error -> {
                    _state.value = _state.value.copy(
                        isChartLoading = false,
                        chartError = result.message
                    )
                }
            }
        }
    }

    // --- Price Alerts ---
    fun addAlert(targetPrice: Long, direction: String, karat: String) {
        viewModelScope.launch {
            proDao.insertAlert(PriceAlertEntity(
                targetPrice = targetPrice,
                direction = direction,
                karat = karat
            ))
        }
    }

    fun deleteAlert(alert: PriceAlertEntity) {
        viewModelScope.launch {
            proDao.deleteAlert(alert)
        }
    }

    fun toggleAlert(alert: PriceAlertEntity) {
        viewModelScope.launch {
            proDao.setAlertActive(alert.id, !alert.isActive)
        }
    }

    fun checkAlerts(currentPrice: BigDecimal, karat: String) {
        val alerts = _state.value.alerts.filter { it.isActive && it.karat == karat }
        for (alert in alerts) {
            val triggered = if (alert.direction == "ABOVE") {
                currentPrice.toLong() >= alert.targetPrice
            } else {
                currentPrice.toLong() <= alert.targetPrice
            }
            if (triggered) {
                viewModelScope.launch {
                    proDao.deactivateAlert(alert.id, System.currentTimeMillis())
                }
                val dirText = if (alert.direction == "ABOVE") "بالا رفتن" else "پایین آمدن"
                val karatText = if (karat == "18") "۱۸" else "۲۴"
                onAlertTriggered(
                    "هشدار قیمت طلا",
                    "قیمت طلای $karatText عیار به ${NumberFormatter.formatNumber(alert.targetPrice)} تومان رسید ($dirText)"
                )
            }
        }
    }

    // --- Gold Assets ---
    fun addAsset(name: String, weightGrams: Double, buyPricePerGram: Long, buyDate: Long) {
        viewModelScope.launch {
            proDao.insertAsset(GoldAssetEntity(
                name = name,
                weightGrams = weightGrams,
                buyPricePerGram = buyPricePerGram,
                buyDate = buyDate
            ))
        }
    }

    fun deleteAsset(asset: GoldAssetEntity) {
        viewModelScope.launch {
            proDao.deleteAsset(asset)
        }
    }

    fun calculateProfitLoss(currentPricePerGram: BigDecimal): ProfitLossResult {
        val assets = _state.value.assets
        if (assets.isEmpty()) return ProfitLossResult()

        var totalBuyValue = BigDecimal.ZERO
        var totalCurrentValue = BigDecimal.ZERO

        for (asset in assets) {
            val weight = BigDecimal(asset.weightGrams)
            val buyPrice = BigDecimal(asset.buyPricePerGram)
            totalBuyValue = totalBuyValue.add(weight.multiply(buyPrice))
            totalCurrentValue = totalCurrentValue.add(weight.multiply(currentPricePerGram))
        }

        val profitLoss = totalCurrentValue.subtract(totalBuyValue)
        val percent = if (totalBuyValue.compareTo(BigDecimal.ZERO) > 0) {
            profitLoss.multiply(BigDecimal("100")).divide(totalBuyValue, 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        return ProfitLossResult(
            totalBuyValue = totalBuyValue,
            totalCurrentValue = totalCurrentValue,
            totalProfitLoss = profitLoss,
            profitLossPercent = percent
        )
    }

    // --- Loan Reminders ---
    fun addReminder(title: String, amount: String, dueDate: Long) {
        viewModelScope.launch {
            proDao.insertReminder(LoanReminderEntity(
                title = title,
                amount = amount,
                dueDate = dueDate
            ))
        }
    }

    fun deleteReminder(reminder: LoanReminderEntity) {
        viewModelScope.launch {
            proDao.deleteReminder(reminder)
        }
    }

    fun toggleReminderPaid(reminder: LoanReminderEntity) {
        viewModelScope.launch {
            proDao.setReminderPaid(reminder.id, !reminder.isPaid)
        }
    }

    // --- Best Loans Data (به‌روزرسانی ۱۴۰۵) ---
    val bestLoans = listOf(
        LoanInfo(
            title = "وام قرض‌الحسنه بانک رسالت",
            maxAmount = "تا ۴۰۰ میلیون تومان",
            interestRate = "۲ تا ۴٪",
            duration = "تا ۶۰ ماه",
            guarantee = "بر اساس رتبه اعتبارسنجی (بدون ضامن برای رتبه ممتاز)",
            description = "فرایند دریافت کاملاً آنلاین است. مبلغ و شرایط وام بر اساس میانگین حساب و اعتبارسنجی سامانه مرآت تعیین می‌شود. ثبت تسهیلات از طریق اپلیکیشن آی‌کاپ انجام می‌شود."
        ),
        LoanInfo(
            title = "وام طرح نیلوفر بانک مهر ایران",
            maxAmount = "تا ۵۰۰ میلیون تومان",
            interestRate = "۲ تا ۴٪",
            duration = "تا ۶۰ ماه",
            guarantee = "بر اساس میانگین حساب",
            description = "وام قرض‌الحسنه مناسب اشخاص حقیقی با ایجاد میانگین حساب. یکی از کم‌نرخ‌ترین وام‌های بازار."
        ),
        LoanInfo(
            title = "وام ازدواج بانک مسکن",
            maxAmount = "تا ۳۵۰ میلیون تومان",
            interestRate = "۴٪",
            duration = "تا ۱۲۰ ماه (۱۰ سال)",
            guarantee = "ضامن رسمی",
            description = "وام ازدواج برای زوج‌های جوان با نرخ سود بسیار پایین و بازپرداخت بلندمدت. ارائه سند ازدواج و معرفی ضامن لازم است."
        ),
        LoanInfo(
            title = "وام قرض‌الحسنه بانک سپه",
            maxAmount = "تا ۵۰ میلیون تومان",
            interestRate = "۴٪",
            duration = "تا ۳۶ ماه",
            guarantee = "ضامن رسمی",
            description = "وام قرض‌الحسنه با نرخ سود پایین و نیازمند سپرده‌گذاری قبلی. مناسب برای نیازهای ضروری و کوتاه‌مدت."
        ),
        LoanInfo(
            title = "وام ماهان بانک دی",
            maxAmount = "تا ۱ میلیارد تومان",
            interestRate = "۸٪",
            duration = "تا ۶۰ ماه",
            guarantee = "ضامن رسمی، اعتبارسنجی",
            description = "وام با نرخ سود مناسب و سقف بالا برای اشخاص حقیقی. نیازمند سپرده و رتبه اعتباری مناسب."
        ),
        LoanInfo(
            title = "طرح همتا بانک مسکن",
            maxAmount = "تا ۲۰۰ میلیون تومان",
            interestRate = "۱۵٪",
            duration = "تا ۳۶ ماه",
            guarantee = "ضامن رسمی",
            description = "وام نقدی با سقف متوسط و نرخ سود ۱۵ درصد. مناسب برای نیازهای شخصی و خانوادگی."
        ),
        LoanInfo(
            title = "وام به‌جا بلوبانک",
            maxAmount = "تا ۴۰۰ میلیون تومان",
            interestRate = "۲۰٪",
            duration = "تا ۱۲ ماه",
            guarantee = "ضامن رسمی، اعتبارسنجی",
            description = "وام نئوبانک بدون نیاز به مراجعه حضوری. دریافت کاملاً آنلاین با ضامن رسمی. مناسب برای نیازهای فوری."
        ),
        LoanInfo(
            title = "طرح باران بانک کشاورزی",
            maxAmount = "تا ۱۰۰ میلیون تومان",
            interestRate = "۱۲٪",
            duration = "تا ۲۴ ماه",
            guarantee = "ضامن رسمی",
            description = "تسهیلات با پشتوانه میانگین حساب و نرخ سود ۱۲ درصد. مناسب برای کشاورزان و بهره‌برداران بخش کشاورزی."
        )
    )

    // --- Gold Gift Links ---
    data class GoldGiftLink(val name: String, val url: String)

    val goldGiftLinks = listOf(
        GoldGiftLink("دریافت طلای هدیه از میلی", "https://milli.gold/app/sign-up?referralCode=milli-ivg8r"),
        GoldGiftLink("دریافت طلای هدیه از ملی گلد", "https://melligold.com/pwa/account/?ref=MGxmhT3q2oVo"),
        GoldGiftLink("دریافت طلای هدیه از زرپین", "https://zarpin.com/auth/login/?referralCode=B8DJMEHM"),
        GoldGiftLink("دریافت طلای هدیه از طلاین", "https://my.tlyn.ir/register?referral=MYNWZY"),
        GoldGiftLink("دریافت طلای هدیه از طلاسی", "https://talasea.ir/onboarding?r=hVEpOXez"),
        GoldGiftLink("دریافت طلای هدیه از تکنوگلد", "https://app.technogold.gold/?referralCode=4zJLlD"),
        GoldGiftLink("دریافت طلای هدیه از تبدیل", "https://tabdeal.org/auth/register-req?refcode=apr25_3m539x")
    )

    fun formatJalaliDate(timestamp: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        val jalali = JalaliCalendar.gregorianToJalali(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        return jalali.toShortString()
    }
}
