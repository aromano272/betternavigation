package com.aromano.betternavigation.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.Serializable
import java.util.Stack

typealias NavArgs = Serializable

abstract class Graph<Args : NavArgs>(
    val startDestination: Destination<Args>
) : Destination<Args>

interface Destination<Args : NavArgs>

//abstract class Direction<From : Destination<*>>(val to: Destination<*>)
//interface Direction<From : Destination<*>, To : Destination<*>>
abstract class Direction<ToArgs : NavArgs>(
    val to: Destination<ToArgs>,
    val navOption: NavOption? = null
)

sealed interface NavOption
data class PopUpTo(val destination: Destination<*>, val inclusive: Boolean = true) : NavOption


interface NavContext {
//    fun navigate(direction: Direction<D>)

    fun navigate(direction: Direction<Nothing>)
    fun <Args : NavArgs> navigate(direction: Direction<Args>, args: Args)

//    fun <To : Destination<Nothing>> Direction<D, To>.navigate()
//    fun <Args : NavArgs, To : Destination<Args>> Direction<D, To>.navigate(args: Args)
}

interface GraphBuilder<Parent : Destination<*>> {
    fun <Args : NavArgs, G : Graph<Args>> typedGraph(graph: G, builder: GraphBuilder<G>.() -> Unit)
//    fun <D : Destination<Nothing>> typedComposable(
//        destination: D,
//        content: @Composable NavContext<D>.() -> Unit
//    )
    fun <Args : NavArgs, D : Destination<Args>> typedComposable(
        destination: D,
        content: @Composable NavContext.(Args) -> Unit
    )
}

@Composable
fun NavHost(
    navController: NavController,
    startGraph: Graph<Nothing>,
    content: GraphBuilder<Nothing>.() -> Unit,
) {
    var currentNode: BackStackEntry<*, *>? by remember { mutableStateOf(null) }
    navController.currentBackStackEntryChanged = { node ->
        currentNode = node
    }

    val navContext = NavContextImpl(navController)
    currentNode?.content(navContext)

    val rootGraphBuilder = GraphBuilderImpl(navController.rootNode, navController)
    content(rootGraphBuilder)
}

class NavContextImpl(
    private val navController: NavController
) : NavContext {
    override fun navigate(direction: Direction<Nothing>) {
        navController.navigate(direction)
    }

    override fun <Args : NavArgs> navigate(direction: Direction<Args>, args: Args) {
        navController.navigate(direction, args)
    }
}

data class DestinationNode<Args : NavArgs, D : Destination<Args>>(
//    val parent: DestinationNode<*, *>?,
    val destination: D?,
    val content: @Composable (NavContext.(Args) -> Unit)?,
    var children: Set<DestinationNode<*, *>>
) {
    companion object {
        fun createRootNode() = DestinationNode<Nothing, Nothing>(null, null, emptySet())
    }
}

data class BackStackEntry<Args : NavArgs, D : Destination<Args>>(
    val destination: D,
    val args: Args?,
    private val content: @Composable (NavContext.(Args) -> Unit)?,
) {
    @Composable
    fun content(navContext: NavContext) = content?.invoke(navContext, args as Args)
}

class NavController {
    val rootNode = DestinationNode.createRootNode()

    val backStack: Stack<BackStackEntry<*, *>> = Stack()

    var currentBackStackEntryChanged: ((BackStackEntry<*, *>) -> Unit)? = null

    fun navigate(direction: Direction<Nothing>) {
        when (direction.navOption) {
            is PopUpTo -> {
                while (backStack.isNotEmpty() && backStack.peek().destination != direction.navOption.destination) {
                    backStack.pop()
                }
                if (backStack.isNotEmpty() && direction.navOption.inclusive) {
                    backStack.pop()
                }
                if (backStack.peek().destination is Graph<*>) {
                    backStack.pop()
                }
            }
            null -> {}
        }

        if (direction.to is Graph) {
            val graphBackStackEntry = BackStackEntry(
                destination = direction.to,
                args = null,
                content = null
            )
            backStack.push(graphBackStackEntry)

            val graphNode = rootNode.findNodeByDestination(direction.to) ?: run {
                printGraph()
                throw IllegalStateException("Couldn't find ${direction.to} in the above NavController")
            }
            val graphStartDestination = (graphNode.destination as Graph).startDestination
            val graphStartNode = graphNode.findNodeByDestination(graphStartDestination) ?: run {
                printGraph()
                throw IllegalStateException("Couldn't find ${graphStartDestination} in the above NavController")
            }
            val graphStartBackStackEntry = BackStackEntry(
                destination = graphStartNode.destination as Destination<Nothing>,
                args = null,
                content = graphStartNode.content
            )
            backStack.push(graphStartBackStackEntry)
        } else {
            val destinationNode = rootNode.findNodeByDestination(direction.to) ?: run {
                printGraph()
                throw IllegalStateException("Couldn't find ${direction.to} in the above NavController")
            }
            // TODO because we can only search down, we're assuming that there's no duplicate destination in the nodes
            val parentGraph = (backStack.peek().destination as? Graph<*>)
                ?: rootNode.findParentOfNodeByDestination(backStack.peek().destination)?.destination ?: run {
                    printGraph()
                    throw IllegalStateException("Couldn't parent of ${direction.to} in the above NavController")
                }
            val destinationGraph = rootNode.findParentOfNodeByDestination(direction.to)?.destination ?: run {
                printGraph()
                throw IllegalStateException("Couldn't parent of ${direction.to} in the above NavController")
            }

            if (parentGraph != destinationGraph) {
                val graphBackStackEntry = BackStackEntry(
                    destination = destinationGraph,
                    args = null,
                    content = null
                )
                backStack.push(graphBackStackEntry)
            }

            val backStackEntry = BackStackEntry(
                destination = direction.to,
                args = null,
                content = destinationNode.content
            )
            backStack.push(backStackEntry)


            // TODO i gotta come up with some sort of type safe, sealed, graph hierarchy, currently there's the graph hierarchy
            //      defined in the Graph->Destination->Direction definition, but then the user can do whatever in the compose definition
            //      i gotta combine both of these worlds into a single place, ideally a sealed typesafe structure
        }

        currentBackStackEntryChanged?.invoke(backStack.peek())
    }

    fun <Args : NavArgs, To : Destination<Args>> navigate(direction: Direction<Args>, args: Args) {
        TODO("Not yet implemented")
    }


    fun DestinationNode<*, *>.findParentOfNodeByDestination(destination: Destination<*>): DestinationNode<*, *>? {
        if (this.destination == destination) return this
        return this.takeIf { this.children.any { this.findNodeByDestination(destination) != null } }
    }

    fun DestinationNode<*, *>.findNodeByDestination(destination: Destination<*>): DestinationNode<*, *>? {
        if (this.destination == destination) return this
        return this.children.firstOrNull { this.findNodeByDestination(destination) != null }
    }

    fun printGraph() {
        printNode(0, rootNode)
    }

    fun printNode(depth: Int, node: DestinationNode<*, *>) {
        val firstPrefix = "     ".repeat(depth)
        val secondPrefix = "|--- "
        print(firstPrefix)
        print(secondPrefix)
        println(node.destination?.debugName() ?: "ROOT")
        node.children.forEach {
            printNode(depth + 1, it)
        }
    }

    fun Destination<*>.debugName() = this::class.simpleName
}

class GraphBuilderImpl<ParentArgs : NavArgs, Parent : Destination<ParentArgs>>(
    private val parentNode: DestinationNode<ParentArgs, Parent>,
    private val navController: NavController
) : GraphBuilder<Parent> {
    override fun <Args : NavArgs, G : Graph<Args>> typedGraph(graph: G, builder: GraphBuilder<G>.() -> Unit) {
        val newNode = DestinationNode(graph, null, emptySet())
        parentNode.children += newNode

        val graphBuilder = GraphBuilderImpl(newNode, navController)
        builder(graphBuilder)
    }

//    override fun <D : Destination<Nothing>> typedComposable(
//        destination: D,
//        content: @Composable (NavContext<D>.() -> Unit)
//    ) {
//        val newNode = DestinationNode(parentNode, destination, emptySet())
//        parentNode.children += newNode
//        navController.addNode(newNode)
//
//        val graphBuilder = GraphBuilderImpl<D>(newNode, navController)
//        builder(graphBuilder)
//    }

    override fun <Args : NavArgs, D : Destination<Args>> typedComposable(
        destination: D,
        content: @Composable (NavContext.(Args) -> Unit)
    ) {
        val newNode = DestinationNode(destination, content, emptySet())
        parentNode.children += newNode
    }
}


/*

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