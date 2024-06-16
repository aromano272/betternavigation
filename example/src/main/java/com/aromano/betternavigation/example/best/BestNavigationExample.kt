package com.aromano.betternavigation.example.best

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.aromano.betternavigation.lib.best.*

interface SwapGraph : Graph<SwapGraphDestination<*>>
interface MainGraph : Graph<MainGraphDestination<*>>

sealed interface SwapGraphDestination<Dir : Direction> : Destination<Dir> {
    interface EnterAmount : SwapGraphDestination<EnterAmountDirections>
    interface Confirmation : SwapGraphDestination<ConfirmationDirections>
}
sealed interface MainGraphDestination<Dir : Direction> : Destination<Dir> {
    interface Feed : MainGraphDestination<Nothing>
}

sealed interface EnterAmountDirections : Direction {
    interface GoToConfirmation : EnterAmountDirections
}

sealed interface ConfirmationDirections : Direction {
    interface GoToMainGraph : ConfirmationDirections
}

@Composable
fun initc() {
    NavHost(
        startGraph = SwapGraph::class
    ) {
        composable(SwapGraphDestination.EnterAmount::class) {
            EnterAmount(goToConfirmation = {
                navigate(EnterAmountDirections.GoToConfirmation::class)
            })
        }
        composable(SwapGraphDestination.Confirmation::class) {
            Confirmation(goToMain = {
                navigate(ConfirmationDirections.GoToMainGraph::class)
            })
        }
        composable(MainGraphDestination.Feed::class) {
            Feed()
        }
    }
}

@Composable
fun EnterAmount(goToConfirmation: () -> Unit) {
    Text(text = "EnterAmount")
    Button(onClick = { goToConfirmation() }) {
        Text(text = "GoToConfirmation")
    }
}

@Composable
fun Confirmation(goToMain: () -> Unit) {
    Text(text = "Confirmation")
    Button(onClick = { goToMain() }) {
        Text(text = "GoToMain")
    }
}

@Composable
fun Feed() {
    Text(text = "Feed")
}

