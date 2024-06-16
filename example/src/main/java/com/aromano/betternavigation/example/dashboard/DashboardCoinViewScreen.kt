package com.aromano.betternavigation.example.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import java.io.Serializable

internal data class DashboardCoinViewArgs(
    val asset: String
) : Serializable

@Composable
internal fun DashboardCoinViewScreen(
    args: DashboardCoinViewArgs,
    viewModel: DashboardCoinViewViewModel = getViewModel(args),
    navigateToSwap: () -> Unit
) {
    Column {
        Text("args $args")
        Button(onClick = { navigateToSwap() }) {
            Text("To Swap")
        }
    }
}

internal fun getViewModel(args: DashboardCoinViewArgs): DashboardCoinViewViewModel {
    return DashboardCoinViewViewModel(args)
}

internal class DashboardCoinViewViewModel(
    args: DashboardCoinViewArgs
)