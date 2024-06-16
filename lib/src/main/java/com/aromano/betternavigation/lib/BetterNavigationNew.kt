package com.aromano.betternavigation.lib.new

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.reflect.KClass

interface Destination<out Dir : Direction>
abstract class Graph<out Dest : Destination<*>>(
    val startDestination: Dest,
    val destinations: Set<Dest>
)
abstract class Direction(
    val destination: Destination<*>
)


data class GraphDestPair<Dest : Destination<*>, G : Graph<Dest>>(
    val graph: G,
    val dest: Destination<*>
)

@Composable
fun NavHost(
    startGraph: Graph<*>,
    graphHandlers: Set<GraphHandler<Destination<*>, Graph<*>>>
) {
    val context = object : NavContext<Direction> {
        override fun navigate(direction: Direction) {
            direction.
        }
    }

    var currentGraphAndDestination: GraphDestPair<*, *> by remember {
        mutableStateOf(GraphDestPair(startGraph, startGraph.startDestination))
    }

    graphHandlers.find { it.graph == currentGraphAndDestination.dest }
        ?.handler
        ?.invoke(context, currentGraphAndDestination.dest)
        ?: throw UnsupportedOperationException("Couldn't find handler for graph: $currentGraphAndDestination")

}

//fun <Dest : Destination<*>, G : Graph<Dest>> buildGraphHandler(
//    graph: G,
//    handler: @Composable NavContext.(Dest) -> Unit
//): GraphDestinationHandler<Dest, G> {
//    val context = object : NavContext {}
//
//    return GraphDestinationHandler(graph, handler)
//}

//@Composable
//fun <Dest : Destination<*>, G : Graph<Dest>> rememberGraphAndDestination(
//    graph: G,
//    destination: Dest
//): MutableState<GraphDestPair<*, *>> {
//    val pair = GraphDestPair(graph, destination)
//}

interface NavContext<Dir : Direction> {
    fun navigate(direction: Dir)
}
data class GraphHandler<Dest : Destination<*>, G : Graph<Dest>>(
    val graph: G,
    val handler: (Dest) -> DestinationHandler<*, Dest>
)

open class DestinationHandler<Dir : Direction, Dest : Destination<Dir>>(
    val destination: Dest,
    val handler: @Composable NavContext<Dir>.(Dest) -> Unit
)



