package gold.app.ghahremani.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * دریافت قیمت روز طلا از منابع معتبر ایرانی
 */
object GoldPriceFetcher {

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
     * تمام قیمت‌های تابلو
     * همه قیمت‌ها به تومان
     */
    data class GoldPrices(
        val gold18PerGram: BigDecimal,      // طلای ۱۸ عیار هر گرم
        val gold24PerGram: BigDecimal,      // طلای ۲۴ عیار هر گرم
        val mesghal: BigDecimal,            // مثقال طلا
        val ounce: BigDecimal               // انس جهانی
    )

    /**
     * دریافت قیمت‌های روز طلا
     */
    suspend fun fetchGoldPrice(): PriceResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.tgju.org/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                .header("Accept-Language", "fa-IR,fa;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string()

            if (!response.isSuccessful || html.isNullOrEmpty()) {
                return@withContext PriceResult.Error("خطا در دریافت صفحه")
            }

            // استخراج قیمت‌ها از data-price با data-market-nameslug
            val gold18 = extractPrice(html, "geram18")
            val gold24 = extractPrice(html, "geram24")
            val mesghal = extractPrice(html, "mesghal")

            if (gold18 == null && gold24 == null) {
                return@withContext PriceResult.Error("قیمت طلا یافت نشد")
            }

            val g18 = gold18 ?: gold24!!.multiply(BigDecimal("0.75"))
            val g24 = gold24 ?: g18.divide(BigDecimal("0.75"), 2, java.math.RoundingMode.HALF_UP)
            val mesq = mesghal ?: g24.multiply(BigDecimal("4.6083"))
            // انس = قیمت هر گرم طلای ۲۴ × ۳۱.۱۰۳۵
            val ounce = g24.multiply(BigDecimal("31.1035"))

            PriceResult.Success(
                GoldPrices(
                    gold18PerGram = g18,
                    gold24PerGram = g24,
                    mesghal = mesq,
                    ounce = ounce
                )
            )
        } catch (e: Exception) {
            PriceResult.Error("خطا: ${e.message}")
        }
    }

    /**
     * استخراج قیمت از HTML بر اساس data-market-nameslug
     */
    private fun extractPrice(html: String, nameslug: String): BigDecimal? {
        val pattern = Regex(
            """data-market-nameslug=["']$nameslug["'][^>]*?data-price=["']([\d,]+)["']"""
        )
        val match = pattern.find(html) ?: return null
        val priceStr = match.groupValues[1]
        return parsePrice(priceStr)
    }

    /**
     * تبدیل رشته قیمت به BigDecimal (تومان)
     * قیمت از TGJU به ریال است → تقسیم بر ۱۰
     */
    private fun parsePrice(priceStr: String): BigDecimal? {
        val cleaned = priceStr.replace(",", "").replace("٬", "").trim()
        if (cleaned.isEmpty()) return null

        return try {
            BigDecimal(cleaned).divide(BigDecimal(10), 2, java.math.RoundingMode.HALF_UP)
        } catch (e: Exception) {
            null
        }
    }
}
