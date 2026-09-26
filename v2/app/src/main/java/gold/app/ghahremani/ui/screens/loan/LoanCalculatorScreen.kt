package gold.app.ghahremani.ui.screens.loan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import gold.app.ghahremani.ui.components.NumberTextField
import gold.app.ghahremani.ui.components.PersianDatePicker
import gold.app.ghahremani.ui.theme.LoanPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.util.JalaliCalendar
import gold.app.ghahremani.util.NumberFormatter

/**
 * صفحه ماشین‌حساب وام و سود
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorScreen(
    viewModel: LoanCalculatorViewModel
) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // دیالوگ‌های انتخاب تاریخ
    if (showStartDatePicker) {
        PersianDatePicker(
            initialDate = parseJalali(state.startDate) ?: JalaliCalendar.today(),
            onDateSelected = {
                viewModel.updateStartDate(it.toShortString())
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        PersianDatePicker(
            initialDate = parseJalali(state.endDate) ?: JalaliCalendar.today(),
            onDateSelected = {
                viewModel.updateEndDate(it.toShortString())
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // تب‌های جابجایی بین وام و سود روزشمار
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = state.currentTab == 0,
                onClick = { viewModel.updateTab(0) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("وام اقساطی", fontSize = 13.sp)
            }
            SegmentedButton(
                selected = state.currentTab == 1,
                onClick = { viewModel.updateTab(1) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("سود روزشمار", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.currentTab == 0) {
            LoanInstallmentTab(
                state = state,
                viewModel = viewModel,
                appColors = appColors
            )
        } else {
            DailyInterestTab(
                state = state,
                viewModel = viewModel,
                appColors = appColors,
                showStartDatePicker = { showStartDatePicker = true },
                showEndDatePicker = { showEndDatePicker = true }
            )
        }
    }
}

/**
 * تب وام اقساطی
 */
@Composable
private fun LoanInstallmentTab(
    state: LoanCalculatorState,
    viewModel: LoanCalculatorViewModel,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    // مبلغ وام
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("مبلغ وام", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = state.loanAmount,
                onValueChange = { viewModel.updateLoanAmount(it) },
                label = "مبلغ وام",
                modifier = Modifier.fillMaxWidth(),
                suffix = "تومان"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("نرخ سود سالانه", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = state.annualRate,
                onValueChange = { viewModel.updateAnnualRate(it) },
                label = "نرخ سود سالانه",
                modifier = Modifier.fillMaxWidth(),
                suffix = "٪"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("مدت بازپرداخت", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = state.duration,
                onValueChange = { viewModel.updateDuration(it) },
                label = "مدت",
                modifier = Modifier.fillMaxWidth(),
                suffix = state.durationUnit.displayName
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DurationUnit.values().forEach { unit ->
                    FilterChip(
                        selected = state.durationUnit == unit,
                        onClick = { viewModel.updateDurationUnit(unit) },
                        label = { Text(unit.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LoanPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // روش محاسبه
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("روش محاسبه سود", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LoanMethod.values().forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (state.loanMethod == method) appColors.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { viewModel.updateLoanMethod(method) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = state.loanMethod == method,
                        onClick = { viewModel.updateLoanMethod(method) },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                            selectedColor = LoanPrimary
                        )
                    )
                    Text(
                        method.displayName,
                        fontSize = 14.sp,
                        color = if (state.loanMethod == method) appColors.textPrimary else appColors.textSecondary
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // دکمه محاسبه
    Button(
        onClick = { viewModel.calculateLoan() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LoanPrimary,
            contentColor = Color.White
        )
    ) {
        Text("محاسبه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // نتایج
    AnimatedVisibility(
        visible = state.loanResult != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        state.loanResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("نتیجه محاسبه", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    LoanResultRow("قسط ماهانه", NumberFormatter.formatToman(result.monthlyPayment), "تومان", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    LoanResultRow("مجموع سود", NumberFormatter.formatToman(result.totalInterest), "تومان", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    LoanResultRow("مجموع بازپرداخت", NumberFormatter.formatToman(result.totalPayment), "تومان", appColors)

                    Spacer(modifier = Modifier.height(16.dp))
                    // جدول اقساط
                    Text("جدول اقساط", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // هدر جدول
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LoanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قسط", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                            Text("مبلغ قسط", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("سود", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("اصل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("مانده", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ردیف‌های جدول
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(result.installments) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(JalaliCalendar.toPersianDigits(row.number), fontSize = 11.sp, color = appColors.textPrimary, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                                Text(NumberFormatter.formatToman(row.payment), fontSize = 10.sp, color = appColors.textPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(NumberFormatter.formatToman(row.interest), fontSize = 10.sp, color = appColors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(NumberFormatter.formatToman(row.principal), fontSize = 10.sp, color = appColors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(NumberFormatter.formatToman(row.remaining), fontSize = 10.sp, color = appColors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveCalculation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary, contentColor = Color.White),
                        enabled = !state.isSaved
                    ) {
                        Text(if (state.isSaved) "ذخیره شد ✓" else "ذخیره در تاریخچه", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * تب سود روزشمار
 */
@Composable
private fun DailyInterestTab(
    state: LoanCalculatorState,
    viewModel: LoanCalculatorViewModel,
    appColors: gold.app.ghahremani.ui.theme.AppColors,
    showStartDatePicker: () -> Unit,
    showEndDatePicker: () -> Unit
) {
    // مبلغ اصل
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("مبلغ اصل", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = state.principal,
                onValueChange = { viewModel.updatePrincipal(it) },
                label = "مبلغ اصل",
                modifier = Modifier.fillMaxWidth(),
                suffix = "تومان"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("نرخ سود سالانه", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = state.dailyRate,
                onValueChange = { viewModel.updateDailyRate(it) },
                label = "نرخ سود سالانه",
                modifier = Modifier.fillMaxWidth(),
                suffix = "٪"
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // مبنای روز
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("مبنای محاسبه روز", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayBasis.values().forEach { basis ->
                    FilterChip(
                        selected = state.dayBasis == basis,
                        onClick = { viewModel.updateDayBasis(basis) },
                        label = { Text(basis.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LoanPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // روش انتخاب تاریخ
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("از امروز N روز", fontSize = 14.sp, color = appColors.textPrimary)
                androidx.compose.material3.Switch(
                    checked = state.useDaysFromToday,
                    onCheckedChange = { viewModel.toggleUseDaysFromToday() },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedTrackColor = LoanPrimary
                    )
                )
            }

            if (state.useDaysFromToday) {
                Spacer(modifier = Modifier.height(12.dp))
                NumberTextField(
                    value = state.daysFromToday,
                    onValueChange = { viewModel.updateDaysFromToday(it) },
                    label = "تعداد روز",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = "روز"
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                // تاریخ شروع
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تاریخ شروع:", fontSize = 14.sp, color = appColors.textSecondary)
                    Button(
                        onClick = showStartDatePicker,
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary, contentColor = Color.White)
                    ) {
                        Text(state.startDate.ifEmpty { "انتخاب تاریخ" }, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // تاریخ پایان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تاریخ پایان:", fontSize = 14.sp, color = appColors.textSecondary)
                    Button(
                        onClick = showEndDatePicker,
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary, contentColor = Color.White)
                    ) {
                        Text(state.endDate.ifEmpty { "انتخاب تاریخ" }, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // دکمه محاسبه
    Button(
        onClick = { viewModel.calculateDailyInterest() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LoanPrimary,
            contentColor = Color.White
        )
    ) {
        Text("محاسبه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // نتایج
    AnimatedVisibility(
        visible = state.dailyResult != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        state.dailyResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("نتیجه محاسبه", fontWeight = FontWeight.Bold, color = appColors.textPrimary, fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    LoanResultRow("تعداد روز", JalaliCalendar.toPersianDigits(result.days), "روز", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result.endDate.isNotEmpty()) {
                        LoanResultRow("تاریخ پایان", result.endDate, "", appColors)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    LoanResultRow("کل سود", NumberFormatter.formatToman(result.totalInterest), "تومان", appColors)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(appColors.divider))
                    Spacer(modifier = Modifier.height(12.dp))
                    LoanResultRow("مبلغ نهایی", NumberFormatter.formatToman(result.finalAmount), "تومان", appColors)

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveCalculation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary, contentColor = Color.White),
                        enabled = !state.isSaved
                    ) {
                        Text(if (state.isSaved) "ذخیره شد ✓" else "ذخیره در تاریخچه", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * ردیف نتیجه وام
 */
@Composable
private fun LoanResultRow(
    label: String,
    value: String,
    unit: String,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = appColors.textSecondary, fontSize = 14.sp)
        Text("$value $unit", color = appColors.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

/**
 * تبدیل رشته تاریخ شمسی به JalaliDate
 */
private fun parseJalali(dateStr: String): JalaliCalendar.JalaliDate? {
    if (dateStr.isEmpty()) return null
    val english = JalaliCalendar.toEnglishDigits(dateStr)
    val parts = english.split("/")
    if (parts.size != 3) return null
    return try {
        JalaliCalendar.JalaliDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (e: Exception) {
        null
    }
}
