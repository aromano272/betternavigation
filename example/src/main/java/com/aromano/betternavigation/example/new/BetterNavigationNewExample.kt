package com.aromano.betternavigation.example.new

import androidx.compose.runtime.Composable
import com.aromano.betternavigation.example.dashboard.DashboardCoinViewArgs
import com.aromano.betternavigation.example.dashboard.DashboardCoinViewScreen
import com.aromano.betternavigation.example.dashboard.DashboardOverviewScreen
import com.aromano.betternavigation.example.swap.SwapConfirmationArgs
import com.aromano.betternavigation.example.swap.SwapConfirmationScreen
import com.aromano.betternavigation.example.swap.SwapEnterAmountScreen
import com.aromano.betternavigation.example.swap.SwapOrderStatusArgs
import com.aromano.betternavigation.lib.GraphBuilder
import com.aromano.betternavigation.lib.new.*

internal object SwapGraph : Graph<SwapGraphDestination<*>>(
    startDestination = SwapGraphDestination.EnterAmount,
    destinations = emptySet()
)

sealed interface SwapGraphDestination<Dir : Direction> : Destination<Dir> {
    data object EnterAmount : SwapGraphDestination<EnterAmountDirection>
    sealed class EnterAmountDirection(destination: Destination<*>) : Direction(destination) {
        data object ToConfirmation : EnterAmountDirection(Confirmation)
        data object ToFinal : EnterAmountDirection(Final)
    }

    data object Confirmation : SwapGraphDestination<ConfirmationDirection>
    sealed class ConfirmationDirection(destination: Destination<*>) : Direction(destination) {
        data object ToOtherGraph : ConfirmationDirection(OtherGraph)
        data object ToOrderStatus : ConfirmationDirection
    }

    data object Final : SwapGraphDestination<Nothing>
}

fun alsjdf() {
    DestinationHandler(SwapGraphDestination.EnterAmount) {
        navigate(SwapGraphDestination.EnterAmountDirection.ToConfirmation)
        navigate(SwapGraphDestination.ConfirmationDirection.ToOrderStatus)
    }
}

object ConfirmationDestinationHandler : DestinationHandler<
        SwapGraphDestination.ConfirmationDirection,
        SwapGraphDestination.Confirmation
        >(
    destination = SwapGraphDestination.Confirmation,
    handler = {
        SwapEnterAmountScreen(
            navigateToConfirmation = { amount, source, target ->
                navigate(SwapGraphDestination.EnterAmountDirection.ToConfirmation)
                navigate(SwapGraphDestination.ConfirmationDirection.ToOrderStatus)
            }
        )
    }
)

@Composable
fun initnav() {
    NavHost(
        startGraph = SwapGraph,
        graphHandlers = setOf(
            GraphHandler(SwapGraph) { destination ->
                when (destination) {
                    is SwapGraphDestination.EnterAmount -> DestinationHandler(destination) {
                        SwapEnterAmountScreen(
                            navigateToConfirmation = { amount, source, target ->
                                navigate(SwapGraphDestination.EnterAmountDirection.ToConfirmation)
                                navigate(SwapGraphDestination.EnterAmountDirection.ToFinal)
                                navigate(SwapGraphDestination.ConfirmationDirection.ToOrderStatus)
                            }
                        )
                    }
                    is SwapGraphDestination.Confirmation -> ConfirmationDestinationHandler
                            as DestinationHandler<*, SwapGraphDestination<*>>
//                    is SwapGraphDestination.Confirmation -> DestinationHandler<
//                            SwapGraphDestination.ConfirmationDirection,
//                            SwapGraphDestination.Confirmation
//                            >(destination) {
//                        SwapEnterAmountScreen(
//                            navigateToConfirmation = { amount, source, target ->
//                                navigate(SwapGraphDestination.EnterAmountDirection.ToConfirmation)
//                                navigate(SwapGraphDestination.ConfirmationDirection.ToOrderStatus)
//                            }
//                        )
//                    }
                }
            }



            GraphDestinationHandler(SwapGraph) { destination ->
                when (destination) {
                    SwapGraphDestination.EnterAmount -> SwapEnterAmountScreen(
                        navigateToConfirmation = { amount, source, target ->
                            navigate(SwapGraphDestination.EnterAmountDirection.ToConfirmation)
                            navigate(SwapGraphDestination.ConfirmationDirection.ToOrderStatus)
                        }
                    )
//                    SwapGraphDestination.Confirmation -> SwapConfirmationScreen(
//                        args = args,
//                        navigateToOrderStatus = { order ->
//
//                        }
//                    )
                }
            }
        )
    )
}


internal object OtherGraph : Graph<String>(startDestination = Stuff) {
    object Stuff : Destination<String>
    object Other : Destination<Nothing>
}

internal object DashboardGraph : Graph<Nothing>(startDestination = Overview) {
    object Overview : Destination<Nothing> {
        object ToCoinView : Direction<DashboardCoinViewArgs>(CoinView)
    }
    object CoinView : Destination<DashboardCoinViewArgs> {
        object ToSwapGraph : Direction<Nothing>(SwapGraph)
    }
//    object CoinView : Destination<DashboardCoinViewArgs, String>
}

internal fun GraphBuilder<Nothing>.swapGraph() {
    typedGraph(
        graph = SwapGraph,
    ) {
        typedComposable(SwapGraph.EnterAmount) {
            SwapEnterAmountScreen(
                navigateToConfirmation = { amount, source, target ->
                    val confirmationArgs = SwapConfirmationArgs(amount, source, target)
//                    SwapGraph.EnterAmount.ToConfirmation.navigate(confirmationArgs)
                    navigate(SwapGraph.EnterAmount.ToConfirmation, confirmationArgs)
                    navigate(SwapGraph.EnterAmount.ToFinal)
                }
            )
        }

        typedComposable(SwapGraph.Confirmation) { args ->
            SwapConfirmationScreen(
                args = args,
                navigateToOrderStatus = { order ->
                    val orderStatusArgs = SwapOrderStatusArgs(order)
                    navigate(SwapGraph.Confirmation.ToOtherGraph, "")
                    navigate(SwapGraph.Confirmation.ToOrderStatus, orderStatusArgs)
                }
            )
        }
    }
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
