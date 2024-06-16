/*
package com.aromano.betternavigation.lib

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.UUID

interface DestinationWithArgsAndResult<T, R> {
    private val baseRoute: String
        get() = this::class.java.name
    val route: String
        get() = "$baseRoute?${NavContext.UUID}={${NavContext.UUID}}"
}
interface DestinationWithArgs<T> : DestinationWithArgsAndResult<T, Nothing>
interface DestinationWithResult<R> : DestinationWithArgsAndResult<Nothing, R> {
    override val route: String
        get() = this::class.java.name
}
interface Destination : DestinationWithResult<Nothing>

abstract class Graph<T, R>(val startDestination: DestinationWithArgsAndResult<T, R>): DestinationWithArgsAndResult<T, R>
abstract class GraphNoArgs<R>(startDestination: DestinationWithResult<R>): Graph<Nothing, R>(startDestination)

class TypedNavGraphBuilder internal constructor(
    internal val graphBuilder: NavGraphBuilder
)

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun TypedNavHost(
    modifier: Modifier = Modifier,
    startGraph: Graph<*, *>,
    debugHook: ((NavController) -> Unit)? = null,
    builder: TypedNavGraphBuilder.() -> Unit
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    if (debugHook != null) {
        debugHook(navController)
    }

    val navContext = remember(navController) { RealNavContext(navController) }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collectLatest {
            val readableBackStack = navController.currentBackStack.value
                .map { it.destination.route ?: "ROOT" }
                .joinToString(" -> ")
            Timber.v("NavBackStack: $readableBackStack")
            val backStackEntriesArgsIds = navController.currentBackStack.value.mapNotNull { backStackEntry ->
                val arguments = backStackEntry.arguments
                val argsId = arguments?.getString(NavContext.UUID)
                argsId
            }

            val idsToRemove = ArgsHolder.keys - backStackEntriesArgsIds
            idsToRemove.forEach { id ->
                ArgsHolder.remove(id)
                Timber.e("REMOVING $id")
            }
        }
    }

    CompositionLocalProvider(LocalNavContext provides navContext) {
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(
                navController = navController,
                startDestination = startGraph.route,
                modifier = modifier,
                route = "ROOT",
                builder = {
                    TypedNavGraphBuilder(this).apply(builder)
                },
            )
        }
    }
}

class DestinationResult<D : DestinationWithArgsAndResult<*, R>, R>(
    val destination: D,
    val result: R
) {
    var wasHandled: Boolean = false
}

private class RealNavContext(private val navController: NavController) : NavContext {
    // TODO: pretty sure this has massive issues with concurrency when setting the wasHandled
    private var pendingResultFlow = MutableStateFlow<DestinationResult<*, *>?>(null)

    override fun <R, D : DestinationWithArgsAndResult<*, R>> listenAndPopDestinationResult(destination: D): Flow<DestinationResult<D, R>> =
        (pendingResultFlow
            .filter { it != null && !it.wasHandled && it.destination == destination } as Flow<DestinationResult<D, R>>)
            .onEach { it.wasHandled = true }

    override fun navigateUp() {
        navController.navigateUp()
    }

    override fun popBackStack(destination: DestinationWithArgsAndResult<Nothing, *>, inclusive: Boolean) {
        navController.popBackStack(destination.route, inclusive)
    }

    override fun navigate(
        destination: DestinationWithArgsAndResult<Nothing, *>,
        navOptions: (TypedNavOptionsBuilder.() -> Unit)?
    ) {
        if (navOptions != null) {
            navController.navigate(
                destination.route,
                builder = {
                    TypedNavOptionsBuilder(this).apply(navOptions)
                }
            )
        } else {
            navController.navigate(destination.route)
        }
    }

    override fun <T> navigate(
        destination: DestinationWithArgsAndResult<T, *>,
        args: T,
        navOptions: (TypedNavOptionsBuilder.() -> Unit)?
    ) {
        val uuid = ArgsHolder.write(args)
        val routeWithArgs = destination.route.replace("{${NavContext.UUID}}", uuid)

        if (navOptions != null) {
            navController.navigate(
                routeWithArgs,
                builder = {
                    TypedNavOptionsBuilder(this).apply(navOptions)
                }
            )
        } else {
            navController.navigate(routeWithArgs)
        }
    }

    override fun <R, D : DestinationWithArgsAndResult<*, R>> navigateBackWithResult(result: DestinationResult<D, R>) {
        pendingResultFlow.value = result
        navController.popBackStack()
    }
}

@Composable
fun <R, D : DestinationWithArgsAndResult<*, R>> NavContext.OnNavigationResultEffect(destination: D, onResult: (R) -> Unit) {
    LaunchedEffect(Unit) {
        listenAndPopDestinationResult(destination).collect { result ->
            onResult(result.result)
        }
    }
}

// TODO: Change to class and scope instance to NavHost so it's possible to use multiple NavHosts in the app
object ArgsHolder {
    private val backingField = mutableMapOf<String, Any?>()
    private val lock = Any()

    val keys: Set<String>
        get() = backingField.keys

    // @returns null if developer error or process death
    fun read(uuid: String): Any? {
        return synchronized(lock) {
            backingField[uuid]
        }
    }

    // @returns uuid String
    fun write(data: Any?): String {
        return synchronized(lock) {
            val uuid = UUID.randomUUID().toString()
            backingField[uuid] = data
            uuid
        }
    }

    fun remove(uuid: String) {
        return synchronized(lock) {
            backingField.remove(uuid)
        }
    }
}

// TODO:  add destination to NavContext so we know R and can skip padding the Destination to the navigateBackWithResult?
interface NavContext {
    fun <R, D : DestinationWithArgsAndResult<*, R>> listenAndPopDestinationResult(destination: D): Flow<DestinationResult<D, R>>

    fun navigateUp()
    fun <R, D : DestinationWithArgsAndResult<*, R>> navigateBackWithResult(result: DestinationResult<D, R>)
    fun popBackStack(destination: DestinationWithArgsAndResult<Nothing, *>, inclusive: Boolean)
    fun navigate(
        destination: DestinationWithArgsAndResult<Nothing, *>,
        navOptions: (TypedNavOptionsBuilder.() -> Unit)? = null
    )
    fun <T> navigate(
        destination: DestinationWithArgsAndResult<T, *>,
        args: T,
        navOptions: (TypedNavOptionsBuilder.() -> Unit)? = null
    )

    companion object {
        const val UUID = "UUID"
    }
}

fun TypedNavGraphBuilder.typedGraph(
    graph: Graph<*, *>,
    builder: TypedNavGraphBuilder.() -> Unit
) = with(graphBuilder) {
    navigation(
        graph.startDestination.route,
        graph.route,
        builder = {
            TypedNavGraphBuilder(this).apply(builder)
        }
    )
}

fun <T> TypedNavGraphBuilder.typedComposable(
    destination: DestinationWithArgsAndResult<T, *>,
    content: @Composable NavContext.(T) -> Unit
) = with(graphBuilder) {
    val args: List<NamedNavArgument> = listOf(navArgument(NavContext.UUID) {})
    composable(
        route = destination.route,
        arguments = args,
        content = {
            val uuid = it.arguments?.getString(NavContext.UUID)!!
            // remember needed because this content block will recompose, and the args might
            // already have been cleared from the holder in case this route is about to be popped
            val args = remember(uuid) {
                @Suppress("UNCHECKED_CAST")
                ArgsHolder.read(uuid) as T
            }
            val navContext = LocalNavContext.current
            content(navContext, args)
        }
    )
}

fun TypedNavGraphBuilder.typedComposable(
    destination: DestinationWithResult<*>,
    content: @Composable NavContext.() -> Unit
) = with(graphBuilder) {
    composable(
        route = destination.route,
        content = {
            val navContext = LocalNavContext.current
            content(navContext)
        }
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun <T> TypedNavGraphBuilder.typedBottomSheet(
    destination: DestinationWithArgsAndResult<T, *>,
    content: @Composable NavContext.(T) -> Unit
) = with(graphBuilder) {
    val args: List<NamedNavArgument> = listOf(navArgument(NavContext.UUID) {})
    bottomSheet(
        route = destination.route,
        arguments = args,
        content = {
            val uuid = it.arguments?.getString(NavContext.UUID)!!
            // remember needed because this content block will recompose, and the args might
            // already have been cleared from the holder in case this route is about to be popped
            val args = remember(uuid) {
                @Suppress("UNCHECKED_CAST")
                ArgsHolder.read(uuid) as T
            }
            val navContext = LocalNavContext.current
            content(navContext, args)
        }
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun TypedNavGraphBuilder.typedBottomSheet(
    destination: DestinationWithResult<*>,
    content: @Composable NavContext.() -> Unit
) = with(graphBuilder) {
    bottomSheet(
        route = destination.route,
        content = {
            val navContext = LocalNavContext.current
            content(navContext)
        }
    )
}

class TypedNavOptionsBuilder internal constructor(
    internal val optionsBuilder: NavOptionsBuilder
)

fun TypedNavOptionsBuilder.popUpTo(
    destination: DestinationWithArgsAndResult<*, *>,
    inclusive: Boolean,
    saveState: Boolean = false
) = with(optionsBuilder) {
    popUpTo(destination.route) {
        this.inclusive = inclusive
        this.saveState = saveState
    }
}


val LocalNavContext = staticCompositionLocalOf<NavContext> { throw IllegalStateException() }
*/