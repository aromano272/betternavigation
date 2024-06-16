package com.aromano.betternavigation.example.dashboard

import com.aromano.betternavigation.example.swap.SwapGraph
import com.aromano.betternavigation.lib.Destination
import com.aromano.betternavigation.lib.Direction
import com.aromano.betternavigation.lib.Graph
import com.aromano.betternavigation.lib.GraphBuilder

internal object DashboardGraph : Graph<Nothing>(startDestination = Overview) {
    object Overview : Destination<Nothing> {
        object ToCoinView : Direction<DashboardCoinViewArgs>(CoinView)
    }
    object CoinView : Destination<DashboardCoinViewArgs> {
        object ToSwapGraph : Direction<Nothing>(SwapGraph)
    }
//    object CoinView : Destination<DashboardCoinViewArgs, String>
}

internal fun GraphBuilder<Nothing>.dashboardGraph(
    // When this pattern of delegating navigation between graph to the parent, BetterNavigationContext
    // needs to be passed as a receiver so the parent has access to navigateTo(Destination) methods
//    navigateFromCoinViewToSwap: NavContext<*>.(Direction<DashboardGraph.CoinView, SwapGraph>) -> Unit
) {
    typedGraph(
        graph = DashboardGraph,
    ) {
        typedComposable(DashboardGraph.Overview) {
            DashboardOverviewScreen(
                navigateToCoinView = { asset ->
                    val coinViewArgs = DashboardCoinViewArgs(asset)
                    navigate(DashboardGraph.Overview.ToCoinView, coinViewArgs)
                }
            )
        }

        typedComposable(DashboardGraph.CoinView) { args ->
            DashboardCoinViewScreen(
                args = args,
                navigateToSwap = {
                    navigate(DashboardGraph.CoinView.ToSwapGraph)
                }
            )
        }
    }
}