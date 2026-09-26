package gold.app.ghahremani.ui.screens.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors

/**
 * صفحه دریافت طلای هدیه
 */
@Composable
fun GoldGiftScreen(viewModel: ProViewModel) {
    val appColors = LocalAppColors.current
    val uriHandler = LocalUriHandler.current

    // کارت هدر با gradient
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🎁 دریافت طلای هدیه رایگان",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "با استفاده از این قابلیت، می‌توانید طلای هدیه رایگان دریافت کنید.\n" +
                    "کافی است روی هر یک از پلتفرم‌های زیر که مورد نظرتان است ضربه بزنید، " +
                    "تا فرایند ثبت‌نام آغاز شود و هدیه خود را دریافت نمایید.\n\n" +
                    "توجه: برای برداشت طلای هدیه، حتماً باید مراحل احراز هویت را تکمیل کنید.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Justify,
                lineHeight = 22.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // دکمه‌های لینک‌های هدیه
    viewModel.goldGiftLinks.forEach { link ->
        Button(
            onClick = { uriHandler.openUri(link.url) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = link.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}
