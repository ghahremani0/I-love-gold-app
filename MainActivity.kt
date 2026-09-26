package gold.app.ghahremani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import gold.app.ghahremani.ui.navigation.AppHeader
import gold.app.ghahremani.ui.screens.gold.GoldCalculatorScreen
import gold.app.ghahremani.ui.screens.gold.GoldCalculatorViewModel
import gold.app.ghahremani.ui.screens.history.HistoryScreen
import gold.app.ghahremani.ui.screens.history.HistoryViewModel
import gold.app.ghahremani.ui.screens.loan.LoanCalculatorScreen
import gold.app.ghahremani.ui.screens.loan.LoanCalculatorViewModel
import gold.app.ghahremani.ui.screens.pro.ProScreen
import gold.app.ghahremani.ui.screens.pro.ProViewModel
import gold.app.ghahremani.ui.screens.splash.SplashScreen
import gold.app.ghahremani.util.NotificationHelper
import gold.app.ghahremani.ui.theme.DarkBackground
import gold.app.ghahremani.ui.theme.GoldCalculatorTheme
import gold.app.ghahremani.ui.theme.LightBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        setContent {
            var isDark by remember { mutableStateOf(false) }

            GoldCalculatorTheme(darkTheme = isDark) {
                MainContent(
                    isDark = isDark,
                    onToggleTheme = { isDark = !isDark }
                )
            }
        }
    }
}

@Composable
fun MainContent(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val app = context.applicationContext as GoldApp

    val goldViewModel: GoldCalculatorViewModel = viewModel {
        GoldCalculatorViewModel(app.database.calculationDao())
    }
    val proViewModel: ProViewModel = viewModel {
        ProViewModel(app.database.proDao()) { title, message ->
            NotificationHelper.showPriceAlert(context, title, message, System.currentTimeMillis().toInt())
        }
    }
    val loanViewModel: LoanCalculatorViewModel = viewModel {
        LoanCalculatorViewModel(app.database.calculationDao())
    }
    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(app.database.calculationDao())
    }

    if (showSplash) {
        SplashScreen(
            onAnimationFinished = { showSplash = false }
        )
    } else {
        // pagerState برای کشیدن بین صفحات
        val pagerState = rememberPagerState(pageCount = { 4 })

        // همگام‌سازی pager با تب‌های هدر - فقط پس از تغییر قطعی
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { page ->
                    if (selectedTab != page) {
                        selectedTab = page
                    }
                }
        }
        LaunchedEffect(selectedTab) {
            if (pagerState.currentPage != selectedTab && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(selectedTab)
            }
        }

        // اشتراک‌گذاری قیمت فعلی تابلو با ProViewModel برای بررسی هشدارها
        val goldState by goldViewModel.state.collectAsState()
        LaunchedEffect(goldState.dashboard) {
            val price = goldViewModel.currentGold18Price
            if (price != null) {
                proViewModel.checkAlerts(price, "18")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DarkBackground else LightBackground)
        ) {
            AppHeader(
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
                isDark = isDark,
                onToggleTheme = onToggleTheme
            )

            // محتوای صفحه‌ها با HorizontalPager برای کشیدن انگشت
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> GoldCalculatorScreen(viewModel = goldViewModel)
                    1 -> LoanCalculatorScreen(viewModel = loanViewModel)
                    2 -> ProScreen(viewModel = proViewModel)
                    3 -> HistoryScreen(viewModel = historyViewModel)
                }
            }
        }
    }
}
