package gold.app.ghahremani.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * هشدار قیمت طلا
 */
@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetPrice: Long,        // قیمت هدف (تومان)
    val direction: String,        // "ABOVE" یا "BELOW"
    val karat: String,            // "18" یا "24"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)

/**
 * دارایی طلا (برای محاسبه سود و زیان)
 */
@Entity(tableName = "gold_assets")
data class GoldAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,             // نام/توضیح
    val weightGrams: Double,      // وزن به گرم
    val buyPricePerGram: Long,    // قیمت خرید هر گرم (تومان)
    val buyDate: Long,            // تاریخ خرید (میلی‌ثانیه)
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * یادآور قسط وام
 */
@Entity(tableName = "loan_reminders")
data class LoanReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,            // عنوان (مثلاً "قسط وام مسکن")
    val amount: String,           // مبلغ قسط
    val dueDate: Long,            // تاریخ سررسید (میلی‌ثانیه)
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
