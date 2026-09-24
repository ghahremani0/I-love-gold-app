package gold.app.ghahremani.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.R
import gold.app.ghahremani.ui.theme.DarkBackground
import gold.app.ghahremani.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

/**
 * صفحه اسپلش با انیمیشن آیکون و نام اپلیکیشن
 */
@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {
    // انیمیشن شفافیت و مقیاس
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.5f) }

    // انیمیشن چرخش ملایم
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        // انیمیشن ورود
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
        // توقف کوتاه
        delay(1200)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // آیکون اپلیکیشن (ماشین حساب طلایی)
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "آیکون ماشین حساب طلا",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha.value)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // نام اپلیکیشن
            Text(
                text = "ماشین حساب طلا و وام",
                color = GoldPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // انیمیشن نقطه‌های زیرنویس
            Text(
                text = "محاسبه سریع و دقیق",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(alpha.value * 0.8f)
            )
        }
    }
}
