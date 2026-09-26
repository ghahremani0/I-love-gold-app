package gold.app.ghahremani.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * دریافت داده‌های نمودار طلا از منبع معتبر
 */
object GoldChartApi {

    private const val API_BASE = "https://raz-analytics.c154.darkube.ir"
    private const val ASSET = "gold18"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    data class ChartPoint(
        val time: Long,        // timestamp in milliseconds
        val price: BigDecimal  // قیمت به تومان
    )

    data class ChartSummary(
        val todayMin: BigDecimal,
        val todayMax: BigDecimal,
        val todayAvg: BigDecimal,
        val todayStart: BigDecimal,
        val changeAmount: BigDecimal,
        val changePercent: BigDecimal,
        val changeType: String,
        val weekChangePercent: BigDecimal,
        val weekChangeType: String,
        val monthChangePercent: BigDecimal,
        val monthChangeType: String
    )

    data class ChartData(
        val points: List<ChartPoint>,
        val summary: ChartSummary
    )

    sealed class ChartResult {
        data class Success(val data: ChartData) : ChartResult()
        data class Error(val message: String) : ChartResult()
    }

    /**
     * دریافت داده‌های نمودار ۷ روزه
     */
    suspend fun fetchChartHistory(range: String = "7d"): ChartResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$API_BASE/v1/assets/$ASSET/chart?range=$range")
                .header("Accept", "application/json")
                .header("Origin", "https://exiraz.com")
                .header("Referer", "https://exiraz.com/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrEmpty()) {
                return@withContext ChartResult.Error("خطا در دریافت نمودار")
            }

            val json = JSONObject(body)
            val chartArray = json.optJSONArray("chart")
                ?: return@withContext ChartResult.Error("داده نمودار یافت نشد")

            val summary = json.optJSONObject("asset_summary")
            val tv = json.optJSONObject("tv")
            val ten = BigDecimal(10)

            val points = mutableListOf<ChartPoint>()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            for (i in 0 until chartArray.length()) {
                val item = chartArray.optJSONObject(i) ?: continue
                val timeStr = item.optString("time", "")
                val priceIrr = item.optLong("price", 0)
                if (priceIrr == 0L) continue

                try {
                    val date = sdf.parse(timeStr)
                    val priceToman = BigDecimal(priceIrr).divide(ten, 0, RoundingMode.HALF_UP)
                    points.add(ChartPoint(date.time, priceToman))
                } catch (e: Exception) {
                    continue
                }
            }

            if (points.isEmpty()) {
                return@withContext ChartResult.Error("داده نمودار خالی است")
            }

            val chartSummary = ChartSummary(
                todayMin = BigDecimal(summary?.optLong("today_minimum", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                todayMax = BigDecimal(summary?.optLong("today_maximum", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                todayAvg = BigDecimal(summary?.optLong("today_average", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                todayStart = BigDecimal(summary?.optLong("today_start_price", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                changeAmount = BigDecimal(summary?.optLong("change_amount", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                changePercent = BigDecimal(summary?.optDouble("change_percent", 0.0) ?: 0.0),
                changeType = summary?.optString("change_type", "no_change") ?: "no_change",
                weekChangePercent = BigDecimal(summary?.optDouble("week_change_percent", 0.0) ?: 0.0),
                weekChangeType = summary?.optString("week_change_type", "no_change") ?: "no_change",
                monthChangePercent = BigDecimal(summary?.optDouble("month_change_percent", 0.0) ?: 0.0),
                monthChangeType = summary?.optString("month_change_type", "no_change") ?: "no_change"
            )

            ChartResult.Success(ChartData(points, chartSummary))
        } catch (e: Exception) {
            ChartResult.Error("خطا در اتصال")
        }
    }

    /**
     * ساخت خلاصه از داده‌های قیمت (برای مواقعی که نمودار در دسترس نیست)
     */
    fun buildSummary(prices: GoldPriceFetcher.GoldPrices): ChartSummary {
        return ChartSummary(
            todayMin = prices.todayMin,
            todayMax = prices.todayMax,
            todayAvg = prices.todayAvg,
            todayStart = prices.todayStart,
            changeAmount = prices.changeAmount,
            changePercent = prices.changePercent,
            changeType = prices.changeType,
            weekChangePercent = prices.weekChangePercent,
            weekChangeType = if (prices.weekChangePercent.compareTo(BigDecimal.ZERO) > 0) "increase"
                else if (prices.weekChangePercent.compareTo(BigDecimal.ZERO) < 0) "decrease" else "no_change",
            monthChangePercent = prices.monthChangePercent,
            monthChangeType = if (prices.monthChangePercent.compareTo(BigDecimal.ZERO) > 0) "increase"
                else if (prices.monthChangePercent.compareTo(BigDecimal.ZERO) < 0) "decrease" else "no_change"
        )
    }
}
