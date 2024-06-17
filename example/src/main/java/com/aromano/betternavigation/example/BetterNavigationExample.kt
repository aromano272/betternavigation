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
import com.aromano.betternavigation.example.dashboard.DashboardGraph
import com.aromano.betternavigation.example.dashboard.dashboardGraph
import com.aromano.betternavigation.example.swap.swapGraph
import com.aromano.betternavigation.lib.NavController
import com.aromano.betternavigation.lib.NavHost
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

@Composable
internal fun BetterNavigationExampleScreen() {
    val navController = remember { NavController() }
    var backStack: List<String> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collectLatest {
            val readableBackStack = navController.backStackFlow.value
                .map {
                    it.destination::class.simpleName.orEmpty()
                }
                .also { backStack = it }
                .joinToString(" -> ")
            println("NavBackStack: $readableBackStack")
        }
    }

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

            println(navController.rootNode)
        }

        Text(
            modifier = Modifier.align(Alignment.BottomStart),
            text = backStack.joinToString("\n"),
            color = Color.Red,
        )

        Button(onClick = {
            navController.printGraph()
        }) {
            Text("Button")
        }
    }
}