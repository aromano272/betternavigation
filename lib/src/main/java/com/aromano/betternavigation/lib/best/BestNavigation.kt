package com.aromano.betternavigation.lib.best

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

interface Graph<Dest : Destination<*>> : Destination<Nothing>
interface Destination<Dir : Direction>
interface Direction

interface NavContext<Dir : Direction, Dest : Destination<Dir>> {
    fun navigate(direction: KClass<out Dir>)
}

interface Builder {
    fun <Dir : Direction, Dest : Destination<Dir>> composable(
        dest: KClass<Dest>,
        composable: @Composable NavContext<Dir, Dest>.() -> Unit
    )
}

@Composable
fun NavHost(
    startGraph: KClass<out Graph<*>>,
    builder: Builder.() -> Unit
) {

}
