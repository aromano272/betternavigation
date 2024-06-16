package com.aromano.betternavigation.example.swap

import com.aromano.betternavigation.lib.Destination
import com.aromano.betternavigation.lib.Direction
import com.aromano.betternavigation.lib.Graph
import com.aromano.betternavigation.lib.GraphBuilder
import com.aromano.betternavigation.lib.PopUpTo

internal object SwapGraph : Graph<Nothing>(startDestination = EnterAmount) {
    object EnterAmount : Destination<Nothing> {
        object ToConfirmation : Direction<SwapConfirmationArgs>(Confirmation)
        object ToFinal : Direction<Nothing>(Final)
    }
    object Confirmation : Destination<SwapConfirmationArgs> {
        object ToOtherGraph : Direction<String>(OtherGraph)
        object ToOrderStatus : Direction<SwapOrderStatusArgs>(OrderStatus, PopUpTo(Confirmation))
    }
    object OrderStatus : Destination<SwapOrderStatusArgs>
    object Final : Destination<Nothing>
}

internal object OtherGraph : Graph<String>(startDestination = Stuff) {
    object Stuff : Destination<String>
    object Other : Destination<Nothing>
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

//        typedComposable(SwapGraph.OrderStatus) { args ->
//            SwapOrderStatusScreen(
//                args = args
//            )
//        }
//        typedComposable(SwapGraph.EnterAmount) { args ->
//            SwapEnterAmountScreen(
//                args = args,
//                navigateToConfirmation = { amount, source, target ->
//                    val confirmationArgs = SwapConfirmationArgs(amount, source, target)
//                    navigate(SwapGraph.Confirmation, confirmationArgs)
//                }
//            )
//        }
//
//        typedComposable(SwapGraph.Confirmation) { args ->
//            SwapConfirmationScreen(
//                args = args,
//                navigateToOrderStatus = { order ->
//                    val orderStatusArgs = SwapOrderStatusArgs(order)
//                    navigate(SwapGraph.OrderStatus, orderStatusArgs) {
//                        popUpTo(SwapGraph, inclusive = false)
//                    }
//                }
//            )
//        }
//
//        typedComposable(SwapGraph.OrderStatus) { args ->
//            SwapOrderStatusScreen(
//                args = args
//            )
//        }
    }
}