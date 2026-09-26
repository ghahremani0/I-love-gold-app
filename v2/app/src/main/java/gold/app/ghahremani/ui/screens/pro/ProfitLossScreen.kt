package gold.app.ghahremani.ui.screens.pro

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import gold.app.ghahremani.ui.theme.SuccessGreen
import gold.app.ghahremani.util.NumberFormatter
import java.math.BigDecimal

/**
 * صفحه محاسبه سود و زیان دارایی طلا
 */
@Composable
fun ProfitLossScreen(viewModel: ProViewModel) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var manualCurrentPrice by remember { mutableStateOf("") }

    val currentPrice = state.currentGold18Price ?: BigDecimal.ZERO
    val effectiveCurrentPrice = if (state.currentGold18Price != null) {
        currentPrice
    } else {
        NumberFormatter.parseInput(manualCurrentPrice)
    }

    val result = viewModel.calculateProfitLoss(effectiveCurrentPrice)

    ProHeaderCard(
        icon = "💰",
        title = "محاسبه سود و زیان دارایی",
        description = "دارایی طلای خود را ثبت کنید و بر اساس قیمت فعلی، سود یا زیان خود را ببینید."
    )

    Spacer(modifier = Modifier.height(16.dp))

    // فرم افزودن دارایی
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام (مثلاً: سکه، طلای ۱۸ عیار)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "وزن (گرم)",
                modifier = Modifier.fillMaxWidth(),
                suffix = "گرم"
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberTextField(
                value = buyPrice,
                onValueChange = { buyPrice = it },
                label = "قیمت خرید هر گرم (تومان)",
                modifier = Modifier.fillMaxWidth(),
                suffix = "تومان"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.currentGold18Price != null) {
                Text(
                    text = "قیمت فعلی تابلو: ${NumberFormatter.formatToman(currentPrice)} تومان",
                    fontSize = 12.sp,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "قیمت فعلی تابلو در دسترس نیست. قیمت را دستی وارد کنید:",
                    fontSize = 12.sp,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                NumberTextField(
                    value = manualCurrentPrice,
                    onValueChange = { manualCurrentPrice = it },
                    label = "قیمت فعلی هر گرم (تومان)",
                    modifier = Modifier.fillMaxWidth(),
                    suffix = "تومان"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val w = NumberFormatter.parseInput(weight).toDouble()
                    val p = NumberFormatter.parseInput(buyPrice).toLong()
                    if (w > 0 && p > 0) {
                        viewModel.addAsset(
                            name = if (name.isBlank()) "طلای ثبت‌شده" else name,
                            weightGrams = w,
                            buyPricePerGram = p,
                            buyDate = System.currentTimeMillis()
                        )
                        name = ""
                        weight = ""
                        buyPrice = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("افزودن دارایی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // خلاصه سود و زیان
    if (state.assets.isNotEmpty() && effectiveCurrentPrice.compareTo(BigDecimal.ZERO) > 0) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "خلاصه",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("ارزش خرید کل:", "${NumberFormatter.formatToman(result.totalBuyValue)} تومان", appColors)
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow("ارزش فعلی کل:", "${NumberFormatter.formatToman(result.totalCurrentValue)} تومان", appColors)
                Spacer(modifier = Modifier.height(8.dp))

                val isProfit = result.totalProfitLoss.compareTo(BigDecimal.ZERO) >= 0
                SummaryRow(
                    label = if (isProfit) "سود:" else "زیان:",
                    value = "${NumberFormatter.formatToman(result.totalProfitLoss.abs())} تومان",
                    appColors = appColors,
                    valueColor = if (isProfit) SuccessGreen else ErrorRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "درصد سود/زیان:",
                    value = "${NumberFormatter.formatDecimal(result.profitLossPercent.abs(), 2)}٪",
                    appColors = appColors,
                    valueColor = if (isProfit) SuccessGreen else ErrorRed
                )
            }
        }
    }

    // لیست دارایی‌ها
    if (state.assets.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "دارایی‌های ثبت‌شده",
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        state.assets.forEach { asset ->
            val assetBuyValue = BigDecimal(asset.weightGrams).multiply(BigDecimal(asset.buyPricePerGram))
            val assetCurrentValue = BigDecimal(asset.weightGrams).multiply(effectiveCurrentPrice)
            val assetProfit = assetCurrentValue.subtract(assetBuyValue)
            val isProfit = assetProfit.compareTo(BigDecimal.ZERO) >= 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
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
                            text = asset.name,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NumberFormatter.formatDecimal(BigDecimal(asset.weightGrams), 3)} گرم × ${NumberFormatter.formatNumber(asset.buyPricePerGram)} تومان",
                            fontSize = 11.sp,
                            color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isProfit) "سود: ${NumberFormatter.formatToman(assetProfit.abs())} تومان"
                                else "زیان: ${NumberFormatter.formatToman(assetProfit.abs())} تومان",
                            fontSize = 12.sp,
                            color = if (isProfit) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deleteAsset(asset) }
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

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    appColors: gold.app.ghahremani.ui.theme.AppColors,
    valueColor: Color? = null
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
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: appColors.textPrimary
        )
    }
}
