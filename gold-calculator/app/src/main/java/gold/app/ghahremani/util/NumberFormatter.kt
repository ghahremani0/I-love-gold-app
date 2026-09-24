package gold.app.ghahremani.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * ابزار قالب‌بندی اعداد - جداکننده هزارگان و اعداد فارسی
 */
object NumberFormatter {

    // وضعیت نمایش اعداد فارسی یا انگلیسی
    var usePersianDigits = true

    /**
     * قالب‌بندی عدد با جداکننده هزارگان
     * @param value مقدار عددی
     * @param usePersianDigits آیا اعداد فارسی نمایش داده شوند
     * @return رشته قالب‌بندی‌شده
     */
    fun formatNumber(value: Long, usePersianDigits: Boolean = this.usePersianDigits): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = formatter.format(value)
        return if (usePersianDigits) {
            JalaliCalendar.toPersianDigits(formatted)
        } else {
            formatted
        }
    }

    /**
     * قالب‌بندی عدد اعشاری با جداکننده هزارگان
     */
    fun formatDecimal(value: BigDecimal, scale: Int = 0, usePersianDigits: Boolean = this.usePersianDigits): String {
        val rounded = value.setScale(scale, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (scale > 0) "#,##0.${"#".repeat(scale)}" else "#,###"
        val formatter = DecimalFormat(pattern, symbols)
        val formatted = formatter.format(rounded)
        return if (usePersianDigits) {
            JalaliCalendar.toPersianDigits(formatted)
        } else {
            formatted
        }
    }

    /**
     * قالب‌بندی مبلغ به تومان
     */
    fun formatToman(value: BigDecimal, usePersianDigits: Boolean = this.usePersianDigits): String {
        val rounded = value.setScale(0, RoundingMode.HALF_UP).toLong()
        return formatNumber(rounded, usePersianDigits)
    }

    /**
     * قالب‌بندی عدد اعشاری ساده (بدون جداکننده)
     */
    fun formatPlain(value: BigDecimal, scale: Int = 2, usePersianDigits: Boolean = this.usePersianDigits): String {
        val rounded = value.setScale(scale, RoundingMode.HALF_UP).toPlainString()
        return if (usePersianDigits) {
            JalaliCalendar.toPersianDigits(rounded)
        } else {
            rounded
        }
    }

    /**
     * پاک کردن قالب‌بندی و تبدیل به BigDecimal
     * اعداد فارسی/عربی را به انگلیسی تبدیل می‌کند و جداکننده‌ها را حذف می‌کند
     */
    fun parseInput(input: String): BigDecimal {
        val english = JalaliCalendar.toEnglishDigits(input)
        val cleaned = english.replace(",", "").replace("٬", "").trim()
        return if (cleaned.isEmpty() || cleaned == ".") {
            BigDecimal.ZERO
        } else {
            try {
                BigDecimal(cleaned)
            } catch (e: Exception) {
                BigDecimal.ZERO
            }
        }
    }

    /**
     * قالب‌بندی ورودی کاربر هنگام تایپ (۳ رقم ۳ رقم جدا می‌شود)
     */
    fun formatInput(input: String, usePersianDigits: Boolean = this.usePersianDigits): String {
        val english = JalaliCalendar.toEnglishDigits(input)
        val cleaned = english.replace(",", "").replace("٬", "").trim()

        if (cleaned.isEmpty()) return ""

        // جدا کردن بخش صحیح و اعشاری
        val parts = cleaned.split(".")
        val integerPart = parts[0]

        // قالب‌بندی بخش صحیح با جداکننده هزارگان
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,###", symbols)
        val formattedInteger = try {
            formatter.format(integerPart.toLong())
        } catch (e: Exception) {
            integerPart
        }

        val result = if (parts.size > 1) {
            "$formattedInteger.${parts[1]}"
        } else {
            formattedInteger
        }

        return if (usePersianDigits) {
            JalaliCalendar.toPersianDigits(result)
        } else {
            result
        }
    }

    /**
     * گرد کردن مبالغ مالی
     */
    fun roundMoney(value: BigDecimal): BigDecimal {
        return value.setScale(0, RoundingMode.HALF_UP)
    }
}
