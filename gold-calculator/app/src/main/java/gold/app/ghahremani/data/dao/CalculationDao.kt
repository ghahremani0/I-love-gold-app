package gold.app.ghahremani.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import gold.app.ghahremani.data.entity.CalculationEntity
import kotlinx.coroutines.flow.Flow

/**
 * عملیات دیتابیس برای محاسبات
 */
@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<CalculationEntity>>

    @Insert
    suspend fun insert(calculation: CalculationEntity): Long

    @Delete
    suspend fun delete(calculation: CalculationEntity)

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculations")
    suspend fun deleteAll()
}
