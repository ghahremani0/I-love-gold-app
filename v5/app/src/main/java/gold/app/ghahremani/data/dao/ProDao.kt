package gold.app.ghahremani.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import gold.app.ghahremani.data.entity.GoldAssetEntity
import gold.app.ghahremani.data.entity.LoanReminderEntity
import gold.app.ghahremani.data.entity.PriceAlertEntity
import kotlinx.coroutines.flow.Flow

/**
 * عملیات دیتابیس برای قابلیت‌های پرو
 */
@Dao
interface ProDao {

    // --- Price Alerts ---
    @Query("SELECT * FROM price_alerts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Insert
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Delete
    suspend fun deleteAlert(alert: PriceAlertEntity)

    @Query("UPDATE price_alerts SET isActive = 0, triggeredAt = :time WHERE id = :id")
    suspend fun deactivateAlert(id: Long, time: Long)

    @Query("UPDATE price_alerts SET isActive = :active WHERE id = :id")
    suspend fun setAlertActive(id: Long, active: Boolean)

    // --- Gold Assets ---
    @Query("SELECT * FROM gold_assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<GoldAssetEntity>>

    @Insert
    suspend fun insertAsset(asset: GoldAssetEntity): Long

    @Delete
    suspend fun deleteAsset(asset: GoldAssetEntity)

    // --- Loan Reminders ---
    @Query("SELECT * FROM loan_reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<LoanReminderEntity>>

    @Insert
    suspend fun insertReminder(reminder: LoanReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: LoanReminderEntity)

    @Query("UPDATE loan_reminders SET isPaid = :paid WHERE id = :id")
    suspend fun setReminderPaid(id: Long, paid: Boolean)
}
