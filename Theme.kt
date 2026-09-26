package gold.app.ghahremani.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import gold.app.ghahremani.R

// فونت وزیرمتن
val VazirmatnFont = FontFamily(
    Font(R.font.vazirmatn_regular),
    Font(R.font.vazirmatn_medium, androidx.compose.ui.text.font.FontWeight.Medium),
    Font(R.font.vazirmatn_bold, androidx.compose.ui.text.font.FontWeight.Bold)
)

// حالت تم اپلیکیشن
enum class AppThemeMode { LIGHT, DARK }

// تم فعلی اپلیکیشن
val LocalThemeMode = staticCompositionLocalOf { AppThemeMode.LIGHT }

// رنگ‌های سفارشی قابل دسترسی در کل اپ
data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = LightBackground,
        surface = LightSurface,
        card = LightCard,
        textPrimary = TextPrimaryLight,
        textSecondary = TextSecondaryLight,
        divider = DividerColor,
        primary = GoldPrimary,
        primaryContainer = GoldSurface,
        onPrimary = Color.White,
        isDark = false
    )
}

@Composable
fun GoldCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) {
        AppColors(
            background = DarkBackground,
            surface = DarkSurface,
            card = DarkCard,
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            divider = DividerColorDark,
            primary = GoldPrimary,
            primaryContainer = GoldSurfaceDark,
            onPrimary = Color.Black,
            isDark = true
        )
    } else {
        AppColors(
            background = LightBackground,
            surface = LightSurface,
            card = LightCard,
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            divider = DividerColor,
            primary = GoldPrimary,
            primaryContainer = GoldSurface,
            onPrimary = Color.White,
            isDark = false
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = GoldPrimary,
            onPrimary = Color.Black,
            primaryContainer = GoldSurfaceDark,
            onPrimaryContainer = GoldLight,
            secondary = LoanPrimary,
            onSecondary = Color.White,
            background = DarkBackground,
            onBackground = TextPrimaryDark,
            surface = DarkSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = DarkCard,
            onSurfaceVariant = TextSecondaryDark
        )
    } else {
        lightColorScheme(
            primary = GoldDark,
            onPrimary = Color.White,
            primaryContainer = GoldSurface,
            onPrimaryContainer = GoldDark,
            secondary = LoanPrimary,
            onSecondary = Color.White,
            background = LightBackground,
            onBackground = TextPrimaryLight,
            surface = LightSurface,
            onSurface = TextPrimaryLight,
            surfaceVariant = LightCard,
            onSurfaceVariant = TextSecondaryLight
        )
    }

    CompositionLocalProvider(
        LocalThemeMode provides if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        LocalAppColors provides appColors,
        androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
