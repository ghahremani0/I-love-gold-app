package gold.app.ghahremani.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * موجودیت محاسبات ذخیره‌شده در دیتابیس
 */
@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,        // نوع محاسبه: "gold", "loan", "daily_interest"
    val title: String,      // عنوان محاسبه
    val result: String,     // نتیجه محاسبه
    val details: String,    // جزئیات محاسبه
    val timestamp: Long     // زمان محاسبه (میلی‌ثانیه)
)
