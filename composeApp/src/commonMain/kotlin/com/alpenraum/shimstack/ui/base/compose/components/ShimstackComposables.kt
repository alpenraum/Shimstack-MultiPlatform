package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpenraum.shimstack.base.BaseViewModel
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.fade
import com.eygraber.compose.placeholder.placeholder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val CARD_DIMENSION = 160.dp
val CARD_MARGIN = 16.dp

@Composable
fun AttachToLifeCycle(viewModel: BaseViewModel) {
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color
) {
    val targetThickness =
        if (thickness == Dp.Hairline) {
            (1f / LocalDensity.current.density).dp
        } else {
            thickness
        }
    Box(
        modifier
            .fillMaxHeight()
            .width(targetThickness)
            .background(color = color)
    )
}

@Composable
fun CardWithPlaceholder(
    showPlaceholder: Boolean,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier =
            modifier.placeholder(
                visible = showPlaceholder,
                highlight = PlaceholderHighlight.fade(),
                color = placeholderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        content = content
    )
}

@Composable
fun ShimstackCard(
    modifier: Modifier = Modifier,
    shape: Shape = shimstackRoundedCornerShape(),
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    content:
        @Composable ColumnScope.(PaddingValues) -> Unit
) {
    Card(
        modifier =
        modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
        content = {
            content(PaddingValues(12.dp))
        },
        shape = shape
    )
}

@Composable
fun LargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = colors,
        content = content
    )
}

@Composable
fun LargeSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        content = content,
        colors = colors
    )
}

@Composable
fun InfoText(
    textRes: StringResource,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontStyle = FontStyle.Italic,
        modifier = modifier
    )
}

fun shimstackRoundedCornerShape() = RoundedCornerShape(20.dp)

@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = shimstackRoundedCornerShape(),
    singleLine: Boolean = true,
    suffix: String? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current
) = OutlinedTextField(
    shape = shape,
    singleLine = singleLine,
    value = value,
    onValueChange = onValueChange,
    suffix = suffix?.let { { Text(text = it) } },
    modifier = modifier,
    label =
        label?.let {
            { Text(text = it) }
        },
    isError = isError,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    readOnly = readOnly,
    trailingIcon = trailingIcon,
    colors = colors,
    textStyle = textStyle
)

/*
TODO : MIGRATE TO TEXTFIELDSTATE
@Composable
fun TextInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    shape: Shape = shimstackRoundedCornerShape(),
    singleLine: Boolean = true,
    suffix: String? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current
) = OutlinedTextField(
    shape = shape,
    lineLimits = if(singleLine) TextFieldLineLimits.SingleLine else  TextFieldLineLimits.Default,
    state = state,
    suffix = suffix?.let { { Text(text = it) } },
    modifier = modifier,
    label =
    label?.let {
        { Text(text = it) }
    },
    isError = isError,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    readOnly = readOnly,
    trailingIcon = trailingIcon,
    colors = colors,
    textStyle = textStyle
)
 */

@Composable
fun ButtonText(
    textRes: StringResource,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    weight: FontWeight = FontWeight.SemiBold
) = Text(
    text = stringResource(textRes),
    modifier = modifier,
    style = style,
    fontWeight = weight
)

@Composable
fun LoadingSpinner() = CircularProgressIndicator()

@Composable
expect fun getScreenWidth(): Dp

@Composable
expect fun getScreenHeight(): Dp