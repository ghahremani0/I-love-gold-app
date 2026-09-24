package gold.app.ghahremani.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gold.app.ghahremani.data.dao.CalculationDao
import gold.app.ghahremani.util.JalaliCalendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ViewModel صفحه تاریخچه
 */
class HistoryViewModel(
    private val dao: CalculationDao
) : ViewModel() {

    val calculations: StateFlow<List<HistoryItem>> = dao.getAllCalculations()
        .map { entities -> entities.map { it.toHistoryItem() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCalculation(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}

/**
 * آیتم تاریخچه برای نمایش
 */
data class HistoryItem(
    val id: Long,
    val type: String,
    val title: String,
    val result: String,
    val details: String,
    val formattedDate: String,
    val typeDisplay: String
)

/**
 * تبدیل CalculationEntity به HistoryItem
 */
fun gold.app.ghahremani.data.entity.CalculationEntity.toHistoryItem(): HistoryItem {
    val typeDisplay = when (type) {
        "gold" -> "طلا"
        "loan" -> "وام"
        "daily_interest" -> "سود روزشمار"
        else -> type
    }

    val date = Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val jalaliDate = JalaliCalendar.gregorianToJalali(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(date)

    val formattedDate = "${jalaliDate.toString()} - ${JalaliCalendar.toPersianDigits(timeStr)}"

    return HistoryItem(
        id = id,
        type = type,
        title = title,
        result = result,
        details = details,
        formattedDate = formattedDate,
        typeDisplay = typeDisplay
    )
}
