package app.tide.loans.ui.person

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModalKeyTest {
    @Test fun openSheetsSurviveBeingSavedAndRestored() {
        listOf(Modal.AddEntry, Modal.EditEntry(7), Modal.EditPerson).forEach {
            assertEquals(it, modalFromKey(modalToKey(it)))
        }
    }

    @Test fun noOpenSheetRestoresAsNone() {
        assertNull(modalFromKey(modalToKey(null)))
    }
}
