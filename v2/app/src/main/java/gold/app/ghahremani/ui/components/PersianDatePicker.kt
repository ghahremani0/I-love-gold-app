package gold.app.ghahremani.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.util.JalaliCalendar
import gold.app.ghahremani.util.JalaliCalendar.JalaliDate

/**
 * دیالوگ انتخاب تاریخ شمسی
 */
@Composable
fun PersianDatePicker(
    initialDate: JalaliDate = JalaliCalendar.today(),
    onDateSelected: (JalaliDate) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.month) }
    var selectedDay by remember { mutableStateOf(initialDate.day) }

    val today = JalaliCalendar.today()
    val monthNames = JalaliCalendar.getMonthNames()
    val daysInMonth = JalaliCalendar.daysInMonth(selectedYear, selectedMonth)

    // لیست روزهای ماه
    val dayList = (1..daysInMonth).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surface,
        title = {
            Text(
                text = "انتخاب تاریخ",
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // نمایش تاریخ انتخاب‌شده
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = JalaliDate(selectedYear, selectedMonth, selectedDay).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                }

                // انتخاب سال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { selectedYear-- }
                    ) { Text("◀", fontSize = 20.sp, color = appColors.primary) }

                    Text(
                        text = "${JalaliCalendar.toPersianDigits(selectedYear)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )

                    TextButton(
                        onClick = { selectedYear++ }
                    ) { Text("▶", fontSize = 20.sp, color = appColors.primary) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // انتخاب ماه
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (selectedMonth > 1) selectedMonth--
                            else { selectedMonth = 12; selectedYear-- }
                        }
                    ) { Text("◀", fontSize = 20.sp, color = appColors.primary) }

                    Text(
                        text = monthNames[selectedMonth - 1],
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appColors.textPrimary
                    )

                    TextButton(
                        onClick = {
                            if (selectedMonth < 12) selectedMonth++
                            else { selectedMonth = 1; selectedYear++ }
                        }
                    ) { Text("▶", fontSize = 20.sp, color = appColors.primary) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // روزهای ماه
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(dayList) { day ->
                        val isSelected = day == selectedDay
                        val isToday = today.year == selectedYear &&
                                today.month == selectedMonth &&
                                today.day == day

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) appColors.primary
                                    else if (isToday) appColors.primaryContainer
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                    color = if (isToday && !isSelected) appColors.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = JalaliCalendar.toPersianDigits(day),
                                fontSize = 14.sp,
                                color = if (isSelected) Color.Black else appColors.textPrimary,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // دکمه امروز
                TextButton(
                    onClick = {
                        selectedYear = today.year
                        selectedMonth = today.month
                        selectedDay = today.day
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("امروز", color = appColors.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(JalaliDate(selectedYear, selectedMonth, selectedDay))
                },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Text("تأیید", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = appColors.textSecondary)
            }
        }
    )
}
