package com.aromano.betternavigation.example

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.aromano.betternavigation.example.dashboard.DashboardGraph
import com.aromano.betternavigation.example.dashboard.dashboardGraph
import com.aromano.betternavigation.example.swap.SwapEnterAmountArgs
import com.aromano.betternavigation.example.swap.SwapGraph
import com.aromano.betternavigation.example.swap.swapGraph
import com.aromano.betternavigation.lib.NavController
import com.aromano.betternavigation.lib.NavHost
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun BetterNavigationExampleScreen() {
    var currentNavController: NavController? by remember { mutableStateOf(null) }
    var backStack: List<String> by remember { mutableStateOf(emptyList()) }
//
    LaunchedEffect(currentNavController) {
        val navController = currentNavController ?: return@LaunchedEffect

        navController.currentBackStackEntryFlow.collectLatest {
            val readableBackStack = navController.backQueue
                .mapNotNull { it.destination.route }
                .map {
                    it
                        .removePrefix("com.aromano.betternavigation.example.dashboard.")
                        .removePrefix("com.aromano.betternavigation.example.swap.")
                        .removePrefix("com.aromano.betternavigation.example.")
                        .removeSuffix("?UUID={UUID}")
                }
                .also { backStack = it }
                .joinToString("\n")
            Timber.v("NavBackStack: $readableBackStack")
        }
    }
    val navController = remember { NavController() }

    Box {
        NavHost(
            navController = navController,
            startGraph = DashboardGraph
        ) {
            dashboardGraph(
//                navigateToSwap = {
//                    navigate(SwapGraph, SwapEnterAmountArgs("BTC"))
//                }
            )

            swapGraph()

            println("BOOM")
            println(navController.rootNode)
        }

        Text(
            modifier = Modifier.align(Alignment.BottomStart),
            text = navController.backStack.joinToString("\n"),
            color = Color.Red,
        )

        Button(onClick = {
            navController.printGraph()
        }) {
            Text("Button")
        }
    }
}