package com.aromano.betternavigation.example.swap

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.io.Serializable

internal data class Order(
    val id: String
) : Serializable

internal data class SwapOrderStatusArgs(
    val order: Order
) : Serializable

@Composable
internal fun SwapOrderStatusScreen(
    args: SwapOrderStatusArgs,
    viewModel: SwapOrderStatusViewModel = getViewModel(args)
) {
    Text("args $args")
}

internal fun getViewModel(args: SwapOrderStatusArgs): SwapOrderStatusViewModel {
    return SwapOrderStatusViewModel(args)
}

internal class SwapOrderStatusViewModel(
    args: SwapOrderStatusArgs
)