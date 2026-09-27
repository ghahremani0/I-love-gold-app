package gold.app.ghahremani.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

/**
 * دریافت قیمت روز طلا از منبع معتبر
 */
object GoldPriceFetcher {

    private const val API_BASE = "https://raz-analytics.c154.darkube.ir"
    private const val ASSET = "gold18"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    sealed class PriceResult {
        data class Success(val prices: GoldPrices) : PriceResult()
        data class Error(val message: String) : PriceResult()
    }

    /**
     * تمام قیمت‌های تابلو - همه به تومان
     */
    data class GoldPrices(
        val gold18PerGram: BigDecimal,      // طلای ۱۸ عیار هر گرم (تومان)
        val gold24PerGram: BigDecimal,      // طلای ۲۴ عیار هر گرم (تومان)
        val mesghal: BigDecimal,            // مثقال طلا (تومان)
        val ounce: BigDecimal,              // انس جهانی (دلار)
        val todayMin: BigDecimal = BigDecimal.ZERO,
        val todayMax: BigDecimal = BigDecimal.ZERO,
        val todayAvg: BigDecimal = BigDecimal.ZERO,
        val todayStart: BigDecimal = BigDecimal.ZERO,
        val changeAmount: BigDecimal = BigDecimal.ZERO,
        val changePercent: BigDecimal = BigDecimal.ZERO,
        val changeType: String = "no_change",
        val weekChangePercent: BigDecimal = BigDecimal.ZERO,
        val monthChangePercent: BigDecimal = BigDecimal.ZERO
    )

    /**
     * دریافت قیمت‌های روز طلا
     */
    suspend fun fetchGoldPrice(): PriceResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$API_BASE/v1/assets/$ASSET/chart?range=1d")
                .header("Accept", "application/json")
                .header("Origin", "https://exiraz.com")
                .header("Referer", "https://exiraz.com/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrEmpty()) {
                return@withContext PriceResult.Error("خطا در دریافت قیمت")
            }

            val json = JSONObject(body)
            val tv = json.optJSONObject("tv")
                ?: return@withContext PriceResult.Error("قیمت‌ها یافت نشد")

            val summary = json.optJSONObject("asset_summary")
            val ten = BigDecimal(10)

            val gold18 = tv.optLong("gold_18_karat", 0)
            val gold24 = tv.optLong("gold_24_karat", 0)
            val mesghal = tv.optLong("price", 0)
            val ounce = tv.optDouble("global_ounce", 0.0)

            if (gold18 == 0L && gold24 == 0L) {
                return@withContext PriceResult.Error("قیمت طلا یافت نشد")
            }

            PriceResult.Success(
                GoldPrices(
                    gold18PerGram = BigDecimal(gold18).divide(ten, 0, RoundingMode.HALF_UP),
                    gold24PerGram = BigDecimal(gold24).divide(ten, 0, RoundingMode.HALF_UP),
                    mesghal = BigDecimal(mesghal).divide(ten, 0, RoundingMode.HALF_UP),
                    ounce = BigDecimal(ounce).setScale(2, RoundingMode.HALF_UP),
                    todayMin = BigDecimal(summary?.optLong("today_minimum", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                    todayMax = BigDecimal(summary?.optLong("today_maximum", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                    todayAvg = BigDecimal(summary?.optLong("today_average", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                    todayStart = BigDecimal(summary?.optLong("today_start_price", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                    changeAmount = BigDecimal(summary?.optLong("change_amount", 0) ?: 0).divide(ten, 0, RoundingMode.HALF_UP),
                    changePercent = BigDecimal(summary?.optDouble("change_percent", 0.0) ?: 0.0),
                    changeType = summary?.optString("change_type", "no_change") ?: "no_change",
                    weekChangePercent = BigDecimal(summary?.optDouble("week_change_percent", 0.0) ?: 0.0),
                    monthChangePercent = BigDecimal(summary?.optDouble("month_change_percent", 0.0) ?: 0.0)
                )
            )
        } catch (e: Exception) {
            PriceResult.Error("خطا در اتصال")
        }
    }
}
