package gold.app.ghahremani.util

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * کلاس تبدیل تاریخ شمسی به میلادی و برعکس
 * الگوریتم: JDF (jdf.scr.ir)
 */
object JalaliCalendar {

    private val jalaliMonthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val persianWeekDays = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    // روزهای هر ماه میلادی (غیر کبیسه)
    private val g_days = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    // روزهای هر ماه شمسی
    private val j_days = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    data class JalaliDate(
        val year: Int,
        val month: Int,
        val day: Int
    ) {
        override fun toString(): String {
            return "${toPersianDigits(day)} ${jalaliMonthNames[month - 1]} ${toPersianDigits(year)}"
        }

        fun toShortString(): String {
            return "${toPersianDigits(year)}/${toPersianDigits(String.format("%02d", month))}/${toPersianDigits(String.format("%02d", day))}"
        }
    }

    /**
     * تبدیل میلادی به شمسی
     */
    fun gregorianToJalali(year: Int, month: Int, day: Int): JalaliDate {
        val gy2 = year - 1600
        val gm2 = month - 1
        val gd2 = day - 1

        var g_day_no = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            g_day_no += g_days[i]
        }

        if (gm2 > 1 && (gy2 % 4 == 0 && gy2 % 100 != 0 || gy2 % 400 == 0)) {
            g_day_no++
        }

        g_day_no += gd2

        var j_day_no = g_day_no - 79

        val j_np = j_day_no / 12053
        j_day_no %= 12053

        var jy = 979 + 33 * j_np + 4 * (j_day_no / 1461)
        j_day_no %= 1461

        if (j_day_no >= 366) {
            jy += (j_day_no - 1) / 365
            j_day_no = (j_day_no - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (j_day_no < 186) {
            jm = 1 + j_day_no / 31
            jd = 1 + j_day_no % 31
        } else {
            jm = 7 + (j_day_no - 186) / 30
            jd = 1 + (j_day_no - 186) % 30
        }

        return JalaliDate(jy, jm, jd)
    }

    /**
     * تبدیل شمسی به میلادی
     */
    fun jalaliToGregorian(jalaliDate: JalaliDate): Triple<Int, Int, Int> {
        val jy = jalaliDate.year - 979
        val jm = jalaliDate.month - 1
        val jd = jalaliDate.day - 1

        var j_day_no = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4

        for (i in 0 until jm) {
            j_day_no += j_days[i]
        }

        j_day_no += jd

        var g_day_no = j_day_no + 79

        var gy = 1600 + 400 * (g_day_no / 146097)
        g_day_no %= 146097

        var leap = true
        if (g_day_no >= 36525) {
            g_day_no--
            gy += 100 * (g_day_no / 36524)
            g_day_no %= 36524
            if (g_day_no >= 365) {
                g_day_no++
            } else {
                leap = false
            }
        }

        gy += 4 * (g_day_no / 1461)
        g_day_no %= 1461

        if (g_day_no >= 366) {
            leap = false
            g_day_no--
            gy += g_day_no / 365
            g_day_no = g_day_no % 365
        }

        // استخراج ماه و روز میلادی
        var gm = 0
        var gd = 0
        var remaining = g_day_no

        for (i in 0 until 12) {
            val daysInThisMonth = if (leap && i == 1) 29 else g_days[i]
            if (remaining < daysInThisMonth) {
                gm = i + 1
                gd = remaining + 1
                break
            }
            remaining -= daysInThisMonth
        }

        return Triple(gy, gm, gd)
    }

    /**
     * سال کبیسه شمسی
     */
    fun isLeapYear(jalaliYear: Int): Boolean {
        val r = jalaliYear % 33
        return r in listOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    /**
     * تعداد روزهای ماه شمسی
     */
    fun daysInMonth(jalaliYear: Int, month: Int): Int {
        return if (month <= 6) 31 else if (month <= 11) 30 else if (isLeapYear(jalaliYear)) 30 else 29
    }

    /**
     * تاریخ امروز به شمسی
     */
    fun today(): JalaliDate {
        val cal = GregorianCalendar()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getMonthName(month: Int): String {
        return if (month in 1..12) jalaliMonthNames[month - 1] else ""
    }

    fun getWeekDayName(year: Int, month: Int, day: Int): String {
        val (gy, gm, gd) = jalaliToGregorian(JalaliDate(year, month, day))
        val cal = GregorianCalendar(gy, gm - 1, gd)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return persianWeekDays[dayOfWeek - 1]
    }

    /**
     * محاسبه تعداد روز بین دو تاریخ شمسی
     */
    fun daysBetween(start: JalaliDate, end: JalaliDate): Int {
        val (sy, sm, sd) = jalaliToGregorian(start)
        val (ey, em, ed) = jalaliToGregorian(end)

        val startCal = GregorianCalendar(sy, sm - 1, sd)
        val endCal = GregorianCalendar(ey, em - 1, ed)

        val diffMillis = endCal.timeInMillis - startCal.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * اضافه کردن روز به تاریخ شمسی
     */
    fun addDays(date: JalaliDate, days: Int): JalaliDate {
        val (gy, gm, gd) = jalaliToGregorian(date)
        val cal = GregorianCalendar(gy, gm - 1, gd)
        cal.add(Calendar.DAY_OF_MONTH, days)
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun toPersianDigits(input: Any): String {
        val englishDigits = "0123456789"
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        return input.toString().map { c ->
            val idx = englishDigits.indexOf(c)
            if (idx >= 0) persianDigits[idx] else c
        }.joinToString("")
    }

    fun toEnglishDigits(input: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val englishDigits = "0123456789"
        return input.map { c ->
            val pIdx = persianDigits.indexOf(c)
            if (pIdx >= 0) englishDigits[pIdx] else {
                val aIdx = arabicDigits.indexOf(c)
                if (aIdx >= 0) englishDigits[aIdx] else c
            }
        }.joinToString("")
    }

    fun getMonthNames(): Array<String> = jalaliMonthNames
}
