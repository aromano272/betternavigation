package com.aromano.betternavigation.example.swap

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.aromano.betternavigation.lib.NavArgs
import java.io.Serializable

internal data class SwapEnterAmountArgs(
    val source: String,
) : NavArgs

@Composable
internal fun SwapEnterAmountScreen(
//    args: SwapEnterAmountArgs,
    navigateToConfirmation: (
        amount: String,
        source: String,
        target: String
    ) -> Unit
) {
    Column {
//        Text("args: $args")
        Button(onClick = { navigateToConfirmation("5.0", "BTC", "ETH") }) {
            Text("To Confirmation")
        }
    }
}