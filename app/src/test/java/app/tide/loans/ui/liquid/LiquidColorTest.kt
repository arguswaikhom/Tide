package app.tide.loans.ui.liquid

import app.tide.loans.ui.theme.Tide
import org.junit.Assert.assertEquals
import org.junit.Test

class LiquidColorTest {
    @Test fun followsTheBalanceDirection() {
        assertEquals(Tide.Blue, settledLiquidColor(100, Tide.Orange))
        assertEquals(Tide.Orange, settledLiquidColor(-100, Tide.Blue))
    }

    @Test fun keepsThePreviousColourWhileDrainingToZero() {
        assertEquals(Tide.Orange, settledLiquidColor(0, Tide.Orange))
        assertEquals(Tide.Blue, settledLiquidColor(0, Tide.Blue))
    }
}
