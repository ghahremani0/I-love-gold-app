package gold.app.ghahremani.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import gold.app.ghahremani.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors

/**
 * صفحه تاریخچه محاسبات
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val calculations by viewModel.calculations.collectAsState()
    val appColors = LocalAppColors.current

    // آیتم‌های تاریخچه (قبلاً در ViewModel تبدیل شده‌اند)
    val items = calculations

    if (items.isEmpty()) {
        // حالت خالی
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_history),
                    contentDescription = "تاریخچه خالی",
                    modifier = Modifier.size(64.dp),
                    tint = GoldPrimary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "هنوز محاسبه‌ای ذخیره نشده",
                    color = appColors.textSecondary,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    HistoryCard(
                        item = item,
                        onDelete = { viewModel.deleteCalculation(item.id) },
                        appColors = appColors
                    )
                }
            }
        }
    }
}

/**
 * کارت تاریخچه
 */
@Composable
private fun HistoryCard(
    item: HistoryItem,
    onDelete: () -> Unit,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    // رنگ بر اساس نوع محاسبه
    val accentColor = when (item.type) {
        "gold" -> GoldPrimary
        "loan" -> gold.app.ghahremani.ui.theme.LoanPrimary
        "daily_interest" -> gold.app.ghahremani.ui.theme.LoanLight
        else -> GoldPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // نوار رنگی سمت راست
            Box(
                modifier = Modifier
                    .size(4.dp, 48.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(12.dp))

            // محتوای کارت
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.typeDisplay,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = item.formattedDate,
                        color = appColors.textSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.result,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary,
                    fontSize = 16.sp
                )
                if (item.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.details,
                        color = appColors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // دکمه حذف
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "حذف",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
