package gold.app.ghahremani.ui.screens.gold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.components.NumberTextField
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.util.NumberFormatter

/**
 * صفحه ماشین‌حساب طلا
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoldCalculatorScreen(
    viewModel: GoldCalculatorViewModel
) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    // به‌روزرسانی خودکار هر ۳۰ ثانیه
    LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // تابلو قیمت‌های زنده - همیشه نمایش داده می‌شود
        DashboardCard(state.dashboard, appColors)
        Spacer(modifier = Modifier.height(12.dp))

        // انتخاب عیار طلا (اول از کاربر پرسیده می‌شود)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.card),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "انتخاب عیار طلا",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // استفاده از FlowRow برای چیدمان تمیز چیپ‌ها
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Karat.values().forEach { karat ->
                        FilterChip(
                            selected = state.selectedKarat == karat,
                            onClick = { viewModel.updateKarat(karat) },
                            label = { Text(karat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // قیمت هر گرم بر اساس عیار انتخاب‌شده
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
                    Text(
                        text = "قیمت هر گرم (${state.selectedKarat.displayName})",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { viewModel.fetchGoldPrice() },
                        enabled = !state.isFetchingPrice,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (state.isFetchingPrice) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Text("دریافت قیمت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                NumberTextField(
                    value = state.goldPrice,
                    onValueChange = { viewModel.updateGoldPrice(it) },
                    label = "قیمت (تومان)",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = "تومان"
                )
                if (state.fetchMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.fetchMessage,
                        fontSize = 12.sp,
                        color = if (state.isFetchingPrice) appColors.textSecondary else GoldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // دکمه جابجایی حالت محاسبه (مستقیم/معکوس)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = state.calcMode == GoldCalcMode.FORWARD,
                onClick = { if (state.calcMode != GoldCalcMode.FORWARD) viewModel.toggleCalcMode() },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("محاسبه مستقیم", fontSize = 13.sp)
            }
            SegmentedButton(
                selected = state.calcMode == GoldCalcMode.REVERSE,
                onClick = { if (state.calcMode != GoldCalcMode.REVERSE) viewModel.toggleCalcMode() },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("محاسبه معکوس", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // وزن یا قیمت نهایی (بسته به حالت)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.card),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.calcMode == GoldCalcMode.FORWARD) {
                    Text(
                        text = "وزن طلا",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberTextField(
                        value = state.weight,
                        onValueChange = { viewModel.updateWeight(it) },
                        label = "وزن",
                        modifier = Modifier.fillMaxWidth(),
                        suffix = state.selectedUnit.displayName
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("واحد وزن:", fontSize = 12.sp, color = appColors.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        WeightUnit.values().forEach { unit ->
                            FilterChip(
                                selected = state.selectedUnit == unit,
                                onClick = { viewModel.updateUnit(unit) },
                                label = { Text(unit.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "قیمت نهایی",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberTextField(
                        value = state.reverseFinalPrice,
                        onValueChange = { viewModel.updateReversePrice(it) },
                        label = "قیمت نهایی (تومان)",
                        modifier = Modifier.fillMaxWidth(),
                        suffix = "تومان"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // اجرت ساخت
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.card),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "اجرت ساخت",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.makingChargeType == MakingChargeType.PERCENT,
                        onClick = { viewModel.updateMakingChargeType(MakingChargeType.PERCENT) },
                        label = { Text("درصدی", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = state.makingChargeType == MakingChargeType.FIXED,
                        onClick = { viewModel.updateMakingChargeType(MakingChargeType.FIXED) },
                        label = { Text("مبلغ ثابت", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                NumberTextField(
                    value = state.makingChargeValue,
                    onValueChange = { viewModel.updateMakingChargeValue(it) },
                    label = if (state.makingChargeType == MakingChargeType.PERCENT) "درصد اجرت" else "مبلغ اجرت (تومان)",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = if (state.makingChargeType == MakingChargeType.PERCENT) "٪" else "تومان"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // سود و مالیات
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.card),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                NumberTextField(
                    value = state.sellerProfitPercent,
                    onValueChange = { viewModel.updateSellerProfit(it) },
                    label = "سود فروشنده (درصد)",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = "٪"
                )
                Spacer(modifier = Modifier.height(12.dp))
                NumberTextField(
                    value = state.taxPercent,
                    onValueChange = { viewModel.updateTaxPercent(it) },
                    label = "مالیات بر ارزش افزوده (درصد)",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = "٪"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // نتایج محاسبه
        AnimatedVisibility(
            visible = state.result.finalPrice.compareTo(java.math.BigDecimal.ZERO) > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "نتیجه محاسبه",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ResultRow("قیمت پایه", NumberFormatter.formatToman(state.result.basePrice), "تومان", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("اجرت ساخت", NumberFormatter.formatToman(state.result.makingCharge), "تومان", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("سود فروشنده", NumberFormatter.formatToman(state.result.profit), "تومان", appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("مالیات", NumberFormatter.formatToman(state.result.tax), "تومان", appColors)

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(appColors.divider)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.calcMode == GoldCalcMode.FORWARD) "قیمت نهایی" else "وزن محاسبه‌شده",
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (state.calcMode == GoldCalcMode.FORWARD)
                                "${NumberFormatter.formatToman(state.result.finalPrice)} تومان"
                            else
                                "${NumberFormatter.formatDecimal(state.result.weight, 3)} گرم",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveCalculation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        enabled = !state.isSaved
                    ) {
                        Text(
                            if (state.isSaved) "ذخیره شد ✓" else "ذخیره در تاریخچه",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * دایره سبز چشمک‌زن - نشانگر به‌روزرسانی خودکار تابلو
 */
@Composable
private fun BlinkingGreenDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF00C853).copy(alpha = alpha))
    )
}

/**
 * کارت تابلو قیمت‌های زنده
 */
@Composable
private fun DashboardCard(
    dashboard: DashboardPrices,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تابلوی قیمت زنده",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 15.sp
                )
                // نشانگر چشمک‌زن به‌روزرسانی خودکار
                BlinkingGreenDot()
            }
            Spacer(modifier = Modifier.height(12.dp))

            // ردیف اول: طلای ۱۸ و ۲۴
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardItem("طلای ۱۸ عیار", dashboard.gold18, appColors, Modifier.weight(1f))
                DashboardItem("طلای ۲۴ عیار", dashboard.gold24, appColors, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            // ردیف دوم: مثقال و انس
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardItem("مثقال طلا", dashboard.mesghal, appColors, Modifier.weight(1f))
                DashboardItem("انس جهانی", dashboard.ounce, appColors, Modifier.weight(1f))
            }

            // تاریخ و ساعت آخرین به‌روزرسانی
            if (dashboard.lastUpdate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(appColors.divider)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "آخرین به‌روزرسانی: ${dashboard.lastUpdate}",
                    fontSize = 11.sp,
                    color = appColors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RowScope.DashboardItem(
    title: String,
    value: String,
    appColors: gold.app.ghahremani.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = appColors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )
        Text(
            text = "تومان",
            fontSize = 10.sp,
            color = appColors.textSecondary
        )
    }
}

/**
 * ردیف نتیجه محاسبه
 */
@Composable
private fun ResultRow(
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
        Text(
            text = label,
            color = appColors.textSecondary,
            fontSize = 14.sp
        )
        Text(
            text = "$value $unit",
            color = appColors.textPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
