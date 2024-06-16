package com.aromano.betternavigation.example.dashboard

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun DashboardOverviewScreen(
    navigateToCoinView: (asset: String) -> Unit
) {
    Button(onClick = { navigateToCoinView("BTC") }) {
        Text("To CoinView")
    }
}