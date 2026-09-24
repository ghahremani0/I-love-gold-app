package gold.app.ghahremani.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import gold.app.ghahremani.ui.theme.LocalAppColors
import gold.app.ghahremani.util.NumberFormatter

/**
 * فیلد متنی عددی با قالب‌بندی خودکار جداکننده هزارگان
 * ورودی کاربر به‌صورت ۳ رقم ۳ رقم جدا می‌شود
 */
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    suffix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val appColors = LocalAppColors.current

    // نگه‌داری موقعیت کرسر
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            // تبدیل اعداد فارسی به انگلیسی و پاک کردن جداکننده‌ها
            val rawInput = NumberFormatter.formatInput(newTextFieldValue.text)
            // فرمت‌بندی مجدد با جداکننده هزارگان
            val formatted = NumberFormatter.formatInput(rawInput)

            textFieldValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )
            // ارسال مقدار خام (بدون جداکننده) به callback
            onValueChange(NumberFormatter.formatInput(rawInput))
        },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        suffix = suffix?.let { { Text(it) } },
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Right
        )
    )
}
