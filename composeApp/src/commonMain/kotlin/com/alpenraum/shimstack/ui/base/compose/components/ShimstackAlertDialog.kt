package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.label_cancel
import shimstackmultiplatform.composeapp.generated.resources.label_ok

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShimstackAlertDialog(
    showDialog: Boolean,
    text: StringResource,
    onConfirm: () -> Unit,
    title: StringResource? = null,
    icon: ImageVector? = null,
    confirmLabel: StringResource = Res.string.label_ok,
    onDismissButton: (() -> Unit)?,
    onDismissDialog: (() -> Unit)? = null,
    dismissLabel: StringResource = Res.string.label_cancel,
    additionalContent: (@Composable () -> Unit)? = null
) {
    AnimatedVisibility(showDialog) {
        BasicAlertDialog(
            onDismissRequest = { onDismissDialog?.invoke() }
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    horizontalAlignment = if (icon != null) Alignment.CenterHorizontally else Alignment.Start,
                    modifier = Modifier.padding(24.dp)
                ) {
                    icon?.let {
                        Icon(
                            it,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    title?.let {
                        Text(
                            stringResource(it),
                            modifier = Modifier.padding(bottom = 16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Text(
                        stringResource(text),
                        modifier = Modifier.padding(bottom = if (additionalContent != null) 16.dp else 0.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    additionalContent?.let {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                            it()
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
                    ) {
                        onDismissButton?.let {
                            TextButton(onClick = it) {
                                ButtonText(dismissLabel, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        TextButton(onClick = onConfirm) {
                            ButtonText(confirmLabel, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}