package gold.app.ghahremani.ui.screens.pro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors

/**
 * صفحه اصلی پرو - نمایش ابزارها و مدیریت ناوبری
 */
@Composable
fun ProScreen(viewModel: ProViewModel) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    // پشتیبانی دکمه بازگشت گوشی
    BackHandler(enabled = state.selectedTool != null) {
        viewModel.selectTool(null)
    }

    val tool = state.selectedTool
    if (tool != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // نوار بالای زیرصفحه
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTool(null) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = tool.title,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (tool) {
                ProTool.GOLD_GIFT -> GoldGiftScreen(viewModel)
                ProTool.GOLD_CHART -> GoldChartScreen(viewModel)
                ProTool.PRICE_ALERT -> PriceAlertScreen(viewModel)
                ProTool.PROFIT_LOSS -> ProfitLossScreen(viewModel)
                ProTool.BEST_LOANS -> BestLoansScreen(viewModel)
                ProTool.LOAN_REMINDER -> LoanReminderScreen(viewModel)
            }
        }
    } else {
        // لیست ابزارها
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFD4AF37),
                                Color(0xFFB8860B)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "ابزارهای پرو",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ابزارهای حرفه‌ای برای مدیریت مالی حرفه‌ای",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // کارت ابزارها - ۲ ستونه
            val tools = ProTool.values()
            for (i in tools.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProToolCard(
                        tool = tools[i],
                        appColors = appColors,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.selectTool(tools[i])
                    }
                    if (i + 1 < tools.size) {
                        ProToolCard(
                            tool = tools[i + 1],
                            appColors = appColors,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.selectTool(tools[i + 1])
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProToolCard(
    tool: ProTool,
    appColors: gold.app.ghahremani.ui.theme.AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tool.icon,
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tool.title,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.description,
                fontSize = 10.sp,
                color = appColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * کارت هدر مشترک برای زیرصفحه‌های پرو
 */
@Composable
fun ProHeaderCard(
    icon: String,
    title: String,
    description: String
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GoldPrimary.copy(alpha = 0.1f))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$icon $title",
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = appColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
