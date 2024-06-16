package com.aromano.betternavigation.lib.evenbetter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aromano.betternavigation.lib.new.GraphDestPair
import com.aromano.betternavigation.lib.new.GraphHandler
import com.aromano.betternavigation.lib.new.NavContext

abstract class Graph(
    vararg destinations: Destination
) : Destination()

abstract class Destination(
    vararg directions: Direction
)

abstract class Direction(
    val destination: Destination
)

interface NavContext<Dir : Direction> {
    fun navigate(direction: Dir)
}
open class DestinationHandler<Dest : Destination>(
    val destination: Dest,
    val handler: @Composable NavContext<Dir>.(Dest) -> Unit
)

interface DestinationHandlerBuilder {
    fun composable(destination: Destination, composable: @Composable () -> Unit)
}

@Composable
fun NavHost(
    startGraph: Graph,
    destinationHandlerBuilder: DestinationHandlerBuilder.() -> Unit
) {
    val context = object : NavContext<com.aromano.betternavigation.lib.new.Direction> {
        override fun navigate(direction: com.aromano.betternavigation.lib.new.Direction) {
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

