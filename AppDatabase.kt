package gold.app.ghahremani.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gold.app.ghahremani.data.dao.CalculationDao
import gold.app.ghahremani.data.dao.ProDao
import gold.app.ghahremani.data.entity.CalculationEntity
import gold.app.ghahremani.data.entity.GoldAssetEntity
import gold.app.ghahremani.data.entity.LoanReminderEntity
import gold.app.ghahremani.data.entity.PriceAlertEntity

/**
 * دیتابیس Room اپلیکیشن
 */
@Database(
    entities = [
        CalculationEntity::class,
        PriceAlertEntity::class,
        GoldAssetEntity::class,
        LoanReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao
    abstract fun proDao(): ProDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gold_calculator_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
