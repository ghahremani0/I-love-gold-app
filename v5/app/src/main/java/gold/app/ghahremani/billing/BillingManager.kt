package gold.app.ghahremani.billing

import android.app.Activity
import android.content.Context
import gold.app.ghahremani.BuildConfig
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.util.IabResult
import ir.myket.billingclient.util.Inventory
import ir.myket.billingclient.util.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدیریت پرداخت درون‌برنامه‌ای مایکت
 * SKU محصول: pro_subscription (اشتراک پرو - مصرف‌نشدنی)
 */
object BillingManager {

    private const val TAG = "BillingManager"
    const val SKU_PRO = "pro_subscription"

    private var iabHelper: IabHelper? = null

    private val _isProUnlocked = MutableStateFlow(false)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    /**
     * راه‌اندازی اتصال به مایکت
     */
    fun setup(context: Context) {
        if (iabHelper != null) return

        val publicKey = BuildConfig.MYKET_PUBLIC_KEY
        val helper = IabHelper(context, publicKey)
        helper.enableDebugLogging(false)

        helper.startSetup(object : IabHelper.OnIabSetupFinishedListener {
            override fun onIabSetupFinished(result: IabResult) {
                if (result.isSuccess) {
                    _isBillingReady.value = true
                    queryInventory()
                } else {
                    _isBillingReady.value = false
                }
            }
        })

        iabHelper = helper
    }

    /**
     * بررسی خریدهای کاربر
     */
    private fun queryInventory() {
        val helper = iabHelper ?: return
        val skus = listOf(SKU_PRO)

        try {
            helper.queryInventoryAsync(true, skus, object : IabHelper.QueryInventoryFinishedListener {
                override fun onQueryInventoryFinished(result: IabResult, inventory: Inventory?) {
                    if (result.isSuccess && inventory != null) {
                        val purchase = inventory.getPurchase(SKU_PRO)
                        _isProUnlocked.value = (purchase != null)
                    }
                }
            })
        } catch (e: Exception) {
            // خطا در بررسی خریدها
        }
    }

    /**
     * شروع فرایند خرید اشتراک پرو
     */
    fun launchPurchaseFlow(activity: Activity) {
        val helper = iabHelper
        if (helper == null || !_isBillingReady.value) {
            _purchaseError.value = "سرویس مایکت در دسترس نیست. لطفاً مطمئن شوید مایکت روی دستگاه نصب است."
            return
        }

        try {
            helper.launchPurchaseFlow(
                activity,
                SKU_PRO,
                object : IabHelper.OnIabPurchaseFinishedListener {
                    override fun onIabPurchaseFinished(result: IabResult, purchase: Purchase?) {
                        if (result.isSuccess && purchase != null) {
                            _isProUnlocked.value = true
                            _purchaseError.value = null
                        } else {
                            _purchaseError.value = if (result.isFailure) {
                                "خرید ناموفق بود. لطفاً دوباره تلاش کنید."
                            } else null
                        }
                    }
                }
            )
        } catch (e: Exception) {
            _purchaseError.value = "خطا در شروع فرایند خرید."
        }
    }

    /**
     * آزادسازی منابع
     */
    fun dispose() {
        iabHelper?.dispose()
        iabHelper = null
        _isBillingReady.value = false
    }

    /**
     * پاک کردن پیام خطا
     */
    fun clearError() {
        _purchaseError.value = null
    }
}
