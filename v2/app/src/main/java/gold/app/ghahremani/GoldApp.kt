package gold.app.ghahremani

import android.app.Application
import gold.app.ghahremani.data.db.AppDatabase
import gold.app.ghahremani.util.NumberFormatter

/**
 * کلاس Application اپلیکیشن
 */
class GoldApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        // تنظیم پیش‌فرض اعداد فارسی
        NumberFormatter.usePersianDigits = true
    }
}
