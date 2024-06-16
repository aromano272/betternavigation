package com.aromano.betternavigation.example.swap

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.io.Serializable

internal data class SwapConfirmationArgs(
    val amount: String,
    val source: String,
    val target: String
) : Serializable

@Composable
internal fun SwapConfirmationScreen(
    args: SwapConfirmationArgs,
    viewModel: SwapConfirmationViewModel = getViewModel(args),
    navigateToOrderStatus: (Order) -> Unit
) {
    Column {
        Text("args $args")
        Button(onClick = { navigateToOrderStatus(Order("1")) }) {
            Text("To OrderStatus")
        }
    }
}

internal fun getViewModel(args: SwapConfirmationArgs): SwapConfirmationViewModel {
    return SwapConfirmationViewModel(args)
}

internal class SwapConfirmationViewModel(
    args: SwapConfirmationArgs
)