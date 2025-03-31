package com.alpenraum.shimstack.ui.base.compose.components.previews

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackAlertDialog
import com.alpenraum.shimstack.ui.base.compose.theme.AppTheme
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.copy_new_bike_ebike
import shimstackmultiplatform.composeapp.generated.resources.copy_setup_outlier_hint
import shimstackmultiplatform.composeapp.generated.resources.duration_label

@Preview
@Composable
fun ShimstackAlertDialogPreview1() =
    AppTheme {
        ShimstackAlertDialog(
            true,
            icon = Icons.Filled.RestoreFromTrash,
            title = Res.string.duration_label,
            text = Res.string.copy_setup_outlier_hint,
            onConfirm = {},
            onDismissButton = {}
        )
    }

@Preview
@Composable
fun ShimstackAlertDialogPreview2() =
    AppTheme {
        ShimstackAlertDialog(
            true,
            icon = null,
            title = Res.string.duration_label,
            text = Res.string.copy_setup_outlier_hint,
            onConfirm = {},
            onDismissButton = {}
        )
    }

@Preview
@Composable
fun ShimstackAlertDialogPreview3() =
    AppTheme {
        ShimstackAlertDialog(
            true,
            icon = null,
            title = null,
            text = Res.string.copy_setup_outlier_hint,
            onConfirm = {},
            onDismissButton = null
        )
    }

@Preview
@Composable
fun ShimstackAlertDialogPreview4() =
    AppTheme {
        ShimstackAlertDialog(
            true,
            icon = null,
            title = null,
            text = Res.string.copy_new_bike_ebike,
            onConfirm = {},
            onDismissButton = null,
            additionalContent = null
        )
    }