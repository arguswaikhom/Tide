package app.tide.loans.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TextFitTest {
    @Test fun amountShrinksToFit() {
        assertEquals(52, amountSp("1,500", 52, 24, 7))
        assertEquals(26, amountSp("12,345,678,901", 52, 24, 7))
        assertEquals(24, amountSp("1,000,000,000,000", 52, 24, 7))
    }

    @Test fun fitPicksTheLargestSizeThatFits() {
        val tenPerSp = { sp: Int -> sp * 10 }
        assertEquals(20, fitSp(baseSp = 20, minSp = 10, maxWidth = 300, widthAt = tenPerSp))
        assertEquals(15, fitSp(baseSp = 20, minSp = 10, maxWidth = 150, widthAt = tenPerSp))
        assertEquals(10, fitSp(baseSp = 20, minSp = 10, maxWidth = 50, widthAt = tenPerSp))
    }
}
