package com.aromano.betternavigation.example.evenbetter

import androidx.compose.runtime.Composable
import com.aromano.betternavigation.lib.evenbetter.Destination
import com.aromano.betternavigation.lib.evenbetter.Direction
import com.aromano.betternavigation.lib.evenbetter.Graph
import com.aromano.betternavigation.lib.evenbetter.NavHost

object SwapGraph : Graph(
    EnterAmountDest,
)
object MainGraph : Graph(
    SummaryDest,
)

object EnterAmountDest : Destination(
    GoToConfirmation
)
object ConfirmationDest : Destination(
    GoToChangeFee,
    GoToCheckout,
)
object ChangeFeeDest : Destination()
object CheckoutDest : Destination(
    GoToMainGraph
)
object SummaryDest : Destination(
    GoToSwapGraph
)

object GoToConfirmation : Direction(ConfirmationDest)
object GoToChangeFee : Direction(ChangeFeeDest)
object GoToCheckout : Direction(CheckoutDest)
object GoToMainGraph : Direction(MainGraph)
object GoToSwapGraph : Direction(SwapGraph)

interface Graph1
interface Destination1

interface SwapGraph1 : Graph1
interface EnterAmountDest1 : Destination1 {
    fun goToConfirmation()
}

@Composable
fun mainf() {
    NavHost(
        startGraph = MainGraph,

    )
}
