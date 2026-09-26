package gold.app.ghahremani.ui.screens.pro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.ErrorRed
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.ui.theme.SuccessGreen
import gold.app.ghahremani.util.GoldChartApi
import gold.app.ghahremani.util.NumberFormatter
import java.math.BigDecimal

/**
 * صفحه نمودار و تحلیل قیمت طلا
 */
@Composable
fun GoldChartScreen(viewModel: ProViewModel) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    LaunchedEffect(Unit) {
        viewModel.fetchChartSummary()
    }

    // کارت عنوان
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD4AF37), Color(0xFFB8860B))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📈 نمودار و تحلیل قیمت طلا",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "روند ۷ روزه قیمت طلای ۱۸ عیار",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val summary = state.chartSummary

    when {
        state.isChartLoading -> {
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldPrimary, strokeWidth = 3.dp, modifier = Modifier.height(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("در حال دریافت داده‌های نمودار...", fontSize = 13.sp, color = appColors.textSecondary)
                    }
                }
            }
        }

        state.chartError.isNotEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⚠️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("اتصال اینترنت را بررسی کنید", fontSize = 14.sp, color = appColors.textPrimary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تلاش مجدد",
                            fontSize = 13.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.fetchChartSummary() }
                        )
                    }
                }
            }
        }

        summary != null && state.chartPoints.isNotEmpty() -> {
            // کارت قیمت فعلی
            val currentPrice = state.currentGold18Price
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "قیمت فعلی طلای ۱۸ عیار",
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (currentPrice != null) "${NumberFormatter.formatToman(currentPrice)} تومان" else "—",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 22.sp
                        )
                        val changeText = if (summary.changeType == "increase") "▲" else if (summary.changeType == "decrease") "▼" else "—"
                        val changeColor = if (summary.changeType == "increase") SuccessGreen else if (summary.changeType == "decrease") ErrorRed else GoldPrimary
                        Text(
                            text = "$changeText ${NumberFormatter.formatDecimal(summary.changeAmount)} تومان (${NumberFormatter.formatDecimal(summary.changePercent.abs(), 2)}٪)",
                            fontWeight = FontWeight.Bold,
                            color = changeColor,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // کارت نمودار خطی
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "نمودار ۷ روزه",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LineChart(
                        points = state.chartPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val firstPrice = state.chartPoints.firstOrNull()?.price ?: BigDecimal.ZERO
                        val lastPrice = state.chartPoints.lastOrNull()?.price ?: BigDecimal.ZERO
                        Text(
                            text = "شروع: ${NumberFormatter.formatToman(firstPrice)}",
                            fontSize = 11.sp,
                            color = appColors.textSecondary
                        )
                        Text(
                            text = "پایان: ${NumberFormatter.formatToman(lastPrice)}",
                            fontSize = 11.sp,
                            color = appColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // کارت تحلیل روند
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "تحلیل روند قیمت",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TrendRow("تغییر امروز", summary.changePercent, summary.changeType, appColors)
                    Spacer(modifier = Modifier.height(12.dp))
                    TrendRow("تغییر هفتگی", summary.weekChangePercent, summary.weekChangeType, appColors)
                    Spacer(modifier = Modifier.height(12.dp))
                    TrendRow("تغییر ماهانه", summary.monthChangePercent, summary.monthChangeType, appColors)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // کارت آمار روزانه
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "آمار قیمت روزانه",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    StatRow("قیمت شروع:", summary.todayStart, appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("حداکثر قیمت:", summary.todayMax, appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("حداقل قیمت:", summary.todayMin, appColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow("میانگین قیمت:", summary.todayAvg, appColors)
                }
            }
        }

        else -> {
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("در حال آماده‌سازی...", fontSize = 13.sp, color = appColors.textSecondary)
                }
            }
        }
    }
}

/**
 * رسم نمودار خطی
 */
@Composable
private fun LineChart(
    points: List<GoldChartApi.ChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val appColors = LocalAppColors.current

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 20f

        val minPrice = points.minOf { it.price }.toFloat()
        val maxPrice = points.maxOf { it.price }.toFloat()
        val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)

        val minTime = points.first().time.toFloat()
        val maxTime = points.last().time.toFloat()
        val timeRange = (maxTime - minTime).coerceAtLeast(1f)

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // رنگ نمودار بر اساس روند کلی
        val firstPrice = points.first().price.toFloat()
        val lastPrice = points.last().price.toFloat()
        val isUp = lastPrice >= firstPrice

        val lineColor = if (isUp) SuccessGreen else ErrorRed
        val fillColor = if (isUp) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)

        // مسیر نمودار
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, point ->
            val x = padding + (point.time.toFloat() - minTime) / timeRange * chartWidth
            val y = padding + chartHeight - (point.price.toFloat() - minPrice) / priceRange * chartHeight

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // مسیر پر کردن
        fillPath.lineTo(padding + chartWidth, padding + chartHeight)
        fillPath.lineTo(padding, padding + chartHeight)
        fillPath.close()

        // رسم ناحیه پر شده
        drawPath(fillPath, color = fillColor)

        // رسم خط
        drawPath(
            path,
            color = lineColor,
            style = Stroke(width = 2.5f)
        )

        // خط راهنما برای قیمت فعلی
        val lastPoint = points.last()
        val lastX = padding + (lastPoint.time.toFloat() - minTime) / timeRange * chartWidth
        val lastY = padding + chartHeight - (lastPoint.price.toFloat() - minPrice) / priceRange * chartHeight

        // نقطه آخر
        drawCircle(
            color = lineColor,
            radius = 4f,
            center = Offset(lastX, lastY)
        )
    }
}

@Composable
private fun TrendRow(
    label: String,
    percent: BigDecimal,
    changeType: String,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    val color = when (changeType) {
        "increase" -> SuccessGreen
        "decrease" -> ErrorRed
        else -> GoldPrimary
    }
    val arrow = when (changeType) {
        "increase" -> "▲"
        "decrease" -> "▼"
        else -> "—"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = appColors.textSecondary
        )
        Text(
            text = "$arrow ${NumberFormatter.formatDecimal(percent.abs(), 2)}٪",
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: BigDecimal,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = appColors.textSecondary
        )
        Text(
            text = "${NumberFormatter.formatToman(value)} تومان",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textPrimary
        )
    }
}
