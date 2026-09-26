package gold.app.ghahremani.ui.navigation

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gold.app.ghahremani.R
import gold.app.ghahremani.ui.theme.DarkBackground
import gold.app.ghahremani.ui.theme.GoldPrimary
import gold.app.ghahremani.ui.theme.LocalAppColors

data class NavItem(val title: String, val index: Int)

val navItems = listOf(
    NavItem("طلا", 0),
    NavItem("سود و وام", 1),
    NavItem("پرو", 2),
    NavItem("تاریخچه", 3)
)

/**
 * هدر اپلیکیشن - نام اپ سمت راست، دکمه تغییر تم سمت چپ
 */
@Composable
fun AppHeader(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val appColors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 12.dp)
    ) {
        // ردیف بالا: نام اپ (راست) + دکمه تغییر تم (چپ)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نام اپلیکیشن (سمت راست - اول در RTL)
            Text(
                text = "ماشین حساب طلا و وام",
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            // دکمه تغییر تم (سمت چپ - دوم در RTL)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .clickable { onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isDark) R.drawable.ic_sun else R.drawable.ic_moon
                    ),
                    contentDescription = "تغییر تم",
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // منوی جابجایی (سه دکمه)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.surface.copy(alpha = if (appColors.isDark) 0.15f else 0.5f))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = selectedIndex == item.index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GoldPrimary else Color.Transparent)
                        .clickable { onItemSelected(item.index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
