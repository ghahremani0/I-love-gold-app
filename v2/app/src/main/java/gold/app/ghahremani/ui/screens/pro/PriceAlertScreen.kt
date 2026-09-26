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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
 * صفحه هشدار قیمت طلا
 */
@Composable
fun PriceAlertScreen(viewModel: ProViewModel) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    var targetPrice by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("ABOVE") }
    var karat by remember { mutableStateOf("18") }

    // درخواست مجوز نوتیفیکیشن
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    ProHeaderCard(
        icon = "🔔",
        title = "هشدار قیمت طلا",
        description = "قیمت مدنظر خود را مشخص کنید. هنگام رسیدن قیمت طلا به این مقدار، نوتیفیکیشن دریافت خواهید کرد."
    )

    Spacer(modifier = Modifier.height(16.dp))

    // فرم ایجاد هشدار
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "قیمت هدف (تومان)",
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = targetPrice,
                onValueChange = { targetPrice = it },
                label = "قیمت هدف",
                modifier = Modifier.fillMaxWidth(),
                suffix = "تومان"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "نوع هشدار:",
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = direction == "ABOVE",
                    onClick = { direction = "ABOVE" },
                    label = { Text("وقتی قیمت بالا رفت", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = direction == "BELOW",
                    onClick = { direction = "BELOW" },
                    label = { Text("وقتی قیمت پایین رفت", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "عیار طلا:",
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = karat == "18",
                    onClick = { karat = "18" },
                    label = { Text("طلای ۱۸ عیار", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = karat == "24",
                    onClick = { karat = "24" },
                    label = { Text("طلای ۲۴ عیار", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val price = NumberFormatter.parseInput(targetPrice).toLong()
                    if (price > 0) {
                        viewModel.addAlert(price, direction, karat)
                        targetPrice = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("افزودن هشدار", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // لیست هشدارها
    Text(
        text = "هشدارهای ثبت‌شده",
        fontWeight = FontWeight.Bold,
        color = appColors.textPrimary,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    state.alerts.forEach { alert ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alert.isActive) appColors.card else appColors.surface
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (alert.direction == "ABOVE")
                            "بالاتر از ${NumberFormatter.formatNumber(alert.targetPrice)} تومان"
                        else
                            "پایین‌تر از ${NumberFormatter.formatNumber(alert.targetPrice)} تومان",
                        fontWeight = FontWeight.Medium,
                        color = appColors.textPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "طلای ${if (alert.karat == "18") "۱۸" else "۲۴"} عیار" +
                            if (!alert.isActive) " - غیرفعال" else "",
                        fontSize = 11.sp,
                        color = if (alert.isActive) GoldPrimary else appColors.textSecondary
                    )
                }
                IconButton(
                    onClick = { viewModel.deleteAlert(alert) }
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
