package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.label_cancel
import shimstackmultiplatform.composeapp.generated.resources.label_ok

@Composable
fun ShimstackAlertDialog(
    showDialog: Boolean,
    title: StringResource? = null,
    text: StringResource,
    onConfirm: () -> Unit,
    confirmLabel: StringResource = Res.string.label_ok,
    onDismissButton: (() -> Unit)?,
    onDismissDialog: (() -> Unit)? = null,
    dismissLabel: StringResource = Res.string.label_cancel
) {
    AnimatedVisibility(showDialog) {
        AlertDialog(
            onDismissRequest = { onDismissDialog?.invoke() },
            title = title?.let { { Text(stringResource(it)) } },
            text = { Text(stringResource(text)) },
            confirmButton = {
                Button(onClick = { onConfirm() }) {
                    Text(stringResource(confirmLabel))
                }
            },
            dismissButton =
                onDismissButton?.let {
                    {
                        Button(onClick = { it() }) {
                            Text(stringResource(dismissLabel))
                        }
                    }
                }
        )
    }
}