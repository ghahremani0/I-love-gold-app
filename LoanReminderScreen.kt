package gold.app.ghahremani.ui.screens.pro

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.components.NumberTextField
import gold.app.ghahremani.ui.theme.ErrorRed
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.util.JalaliCalendar
import gold.app.ghahremani.util.NumberFormatter

/**
 * صفحه یادآور اقساط وام
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanReminderScreen(viewModel: ProViewModel) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // درخواست مجوز نوتیفیکیشن
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    ProHeaderCard(
        icon = "⏰",
        title = "یادآور اقساط وام",
        description = "اطلاعات قسط خود را ثبت کنید تا در زمان سررسید، نوتیفیکیشن دریافت کنید."
    )

    Spacer(modifier = Modifier.height(16.dp))

    // فرم افزودن یادآور
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان (مثلاً: قسط وام مسکن)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "مبلغ قسط (تومان)",
                modifier = Modifier.fillMaxWidth(),
                suffix = "تومان"
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "تاریخ سررسید (شمسی)",
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "انتخاب تاریخ",
                    tint = GoldPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (selectedDateMillis != null) {
                        viewModel.formatJalaliDate(selectedDateMillis!!)
                    } else {
                        "انتخاب تاریخ سررسید"
                    },
                    fontSize = 14.sp,
                    color = if (selectedDateMillis != null) appColors.textPrimary else appColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val dueDate = selectedDateMillis
                    if (dueDate != null && title.isNotBlank()) {
                        // تنظیم ساعت ۱۰ صبح برای یادآوری
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = dueDate
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 10)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)

                        viewModel.addReminder(title, amount, cal.timeInMillis)
                        title = ""
                        amount = ""
                        selectedDateMillis = null
                    }
                },
                enabled = title.isNotBlank() && selectedDateMillis != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("افزودن یادآور", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("تأیید", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("انصراف", color = appColors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // لیست یادآورها
    if (state.reminders.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "یادآورهای ثبت‌شده",
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        state.reminders.forEach { reminder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (reminder.isPaid) appColors.surface else appColors.card
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = reminder.isPaid,
                            onCheckedChange = { viewModel.toggleReminderPaid(reminder) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldPrimary,
                                uncheckedColor = appColors.textSecondary
                            )
                        )
                        Column {
                            Text(
                                text = reminder.title,
                                fontWeight = FontWeight.Bold,
                                color = if (reminder.isPaid) appColors.textSecondary else appColors.textPrimary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val jalaliDate = viewModel.formatJalaliDate(reminder.dueDate)
                            Text(
                                text = "سررسید: $jalaliDate" +
                                    if (reminder.amount.isNotBlank()) " - ${NumberFormatter.parseInput(reminder.amount).toLong()} تومان" else "",
                                fontSize = 11.sp,
                                color = appColors.textSecondary
                            )
                            if (reminder.isPaid) {
                                Text(
                                    text = "✓ پرداخت شد",
                                    fontSize = 11.sp,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { viewModel.deleteReminder(reminder) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "حذف",
                            tint = ErrorRed
                        )
                    }
                }
            }
        }
    }
}
