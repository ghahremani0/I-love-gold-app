package gold.app.ghahremani.ui.screens.pro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors

/**
 * صفحه معرفی بهترین وام‌ها
 */
@Composable
fun BestLoansScreen(viewModel: ProViewModel) {
    val appColors = LocalAppColors.current

    ProHeaderCard(
        icon = "🏦",
        title = "معرفی بهترین وام‌ها",
        description = "لیستی از وام‌های پیشنهادی قابل بررسی با شرایط مربوط به هرکدام"
    )

    Spacer(modifier = Modifier.height(16.dp))

    viewModel.bestLoans.forEach { loan ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
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
                        text = loan.title,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = loan.interestRate,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                LoanDetailRow("سقف مبلغ:", loan.maxAmount, appColors)
                Spacer(modifier = Modifier.height(6.dp))
                LoanDetailRow("مدت بازپرداخت:", loan.duration, appColors)
                Spacer(modifier = Modifier.height(6.dp))
                LoanDetailRow("ضمانت:", loan.guarantee, appColors)

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = loan.description,
                    fontSize = 12.sp,
                    color = appColors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LoanDetailRow(
    label: String,
    value: String,
    appColors: gold.app.ghahremani.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = appColors.textSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
