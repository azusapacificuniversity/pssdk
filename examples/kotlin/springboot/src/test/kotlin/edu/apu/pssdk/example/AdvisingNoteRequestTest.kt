package edu.apu.pssdk.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AdvisingNoteRequestTest {

    private val request = AdvisingNoteRequest(
        EMPLID = "12345678",
        ADVISOR_ID = "87654321",
        SAA_NOTE_SUBJ = "Spring registration",
        SAA_ADV_NOTEDTL = "Met with student to plan Spring 2026 schedule.",
    )

    @Test
    fun `applies the documented defaults`() {
        val payload = request.toCiPayload()

        assertEquals("APU", payload["INSTITUTION"])
        assertEquals("99999", payload["SAA_NOTE_ID"])
        assertEquals("ADVISING", payload["SAA_NOTE_TYPE"])
        assertEquals("GENERAL", payload["SAA_NOTE_SUBTYPE"])
        assertEquals("OP", payload["SAA_NOTE_STATUS"])
        assertEquals(LocalDate.now().toString(), payload["SAA_NOTE_ADD_DT"])
        // SCC_ROW_ADD_OPRID falls back to the advisor EMPLID.
        assertEquals("87654321", payload["SCC_ROW_ADD_OPRID"])
    }

    @Test
    fun `builds the SAA_ADV_NOTEDTL child rowset`() {
        @Suppress("UNCHECKED_CAST")
        val rows = request.toCiPayload()["SAA_ADV_NOTEDTL"] as List<Map<String, Any>>

        assertEquals(1, rows.size)
        assertEquals("1", rows[0]["SAA_ITEM_SEQ"])
        assertEquals("Met with student to plan Spring 2026 schedule.", rows[0]["SAA_NOTE_ITM_LONG"])
    }

    @Test
    fun `child rowset is mutable — CiScroll removes matched rows while populating`() {
        val rows = request.toCiPayload()["SAA_ADV_NOTEDTL"]

        assertTrue(rows is MutableList<*> && rows.removeAt(0) != null)
    }
}
