/*
package com.aromano.betternavigation.lib

import androidx.compose.runtime.Composable
import com.aromano.betternavigation.lib.SwapGraph.Confirmation.navigateToOrderStatus
import com.aromano.betternavigation.lib.SwapGraph.Confirmation.navigateToOtherGraph

interface Graph : Destination
interface Destination
abstract class Direction<Parent>(val toDestination: Destination)

interface NavContext<Parent : Destination> {
    fun navigate(destination: Destination)
    fun navigate(direction: Direction<Parent>)
}

interface GraphBuilder<Parent : Destination> {
    fun <G : Graph> typedGraph(graph: G, builder: GraphBuilder<G>.() -> Unit)
    fun <D : Destination> typedComposable(
        destination: D,
        content: @Composable NavContext<D>.() -> Unit
    )
}

object SwapGraph : Graph {
    object EnterAmount : Destination {
        object ToConfirmation : Direction<EnterAmount>(Confirmation)
    }
    object Confirmation : Destination {
        object ToOtherGraph : Direction<OtherGraph>(OtherGraph)
        object ToOrderStatus : Direction<Confirmation>(OrderStatus)

        fun NavContext<Confirmation>.navigateToOtherGraph() = navigate(OtherGraph)
        fun NavContext<Confirmation>.navigateToOrderStatus() = navigate(OrderStatus)
    }
    object OrderStatus : Destination

}

object OtherGraph : Graph {
    object A : Destination
    object B : Destination
}

fun GraphBuilder<Nothing>.swapGraph() {
    typedGraph(SwapGraph) {
        typedComposable(SwapGraph.EnterAmount) {
            navigate(SwapGraph.EnterAmount.ToConfirmation)
            navigate(SwapGraph.Confirmation.ToOrderStatus)

            navigateToOrderStatus()
        }
        typedComposable(SwapGraph.Confirmation) {
            navigate(SwapGraph.Confirmation.ToOrderStatus)

            navigateToOrderStatus()
            navigateToOtherGraph()
        }
    }
}



// TODO: Maybe swap out the way we build graph with nested classes that are not typed safe to a constructor approach
// as in the constructor of Graph takes in a vararg of Destinations, and those Destinations take in a list of Directions

abstract class Graph<Args : Serializable>(
    val startDestination: Destination<Args>
)

interface Destination<Args : Serializable>
abstract class Direction<Args : Serializable, Target : Destination<Args>>(
    val args: Args
)


interface NavContext {
    fun navigate(graph: Graph<Nothing>)
    fun <Args : Serializable> navigate(graph: Graph<Args>, args: Args)

    fun navigate(destination: Destination<Nothing>)
    fun <Args : Serializable> navigate(destination: Destination<Args>, args: Args)
}



interface GraphBuilder {
    fun <Args : Serializable> typedGraph(graph: Graph<Args>, builder: GraphBuilder.() -> Unit)
    fun <Args : Serializable> typedComposable(
        destination: Destination<Args>,
        content: @Composable NavContext.(Args) -> Unit
    )
}

interface Graph1<Args : Serializable> {
    val startDestination: KClass<Destination1<out Graph1<Args>, Args>>
}


interface Destination1<Graph : Graph1<*>, Args : Serializable>
//interface Direction1<Graph : Graph1<*>, Args : Serializable> {
//    val destination: Destination1<Graph, Args>
//}
abstract class Direction1<Graph : Graph1<*>, Args : Serializable>(
    val destination: Destination1<Graph, Args>
)

data class SwapConfirmationArgs(
    val data: String
) : Serializable

sealed interface SwapGraph : Graph1<Nothing> {
    override val startDestination: Destination1<SwapGraph, Nothing>
        get() = EnterAmount
    val startDestination1: KClass<SwapGraph>
        get() = EnterAmount::class

    sealed interface EnterAmount : Destination1<SwapGraph, Nothing> {
        object ToConfirmation : Direction1<SwapGraph, SwapConfirmationArgs>(Confirmation)
    }
    object Confirmation : Destination1<SwapGraph, SwapConfirmationArgs>
}

sealed interface OtherGraph : Graph1<Nothing> {
    override val startDestination: Destination1<SwapGraph, Nothing>

    sealed interface EnterAmount : Destination1<SwapGraph, Nothing> {
        object ToConfirmation : Direction1<SwapGraph, SwapConfirmationArgs>(Confirmation)
    }
    object Confirmation : Destination1<SwapGraph, SwapConfirmationArgs>
}

fun <Args : Serializable> graph(graph: Graph1<Args>) {}
fun <Args : Serializable> destination(destination: Destination1<Args>) {}

fun main() {
    SwapGraph.Companion.root
    graph(SwapGraph)
    graph(SwapGraph.root)
    SwapGraph.
}


*/