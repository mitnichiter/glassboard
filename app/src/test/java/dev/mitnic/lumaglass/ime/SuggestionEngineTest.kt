package dev.mitnic.lumaglass.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionEngineTest {
    private val engine = SuggestionEngine()

    @Test fun extractsCurrentWord() {
        assertEquals("keyb", engine.currentWord("Try this keyb"))
    }

    @Test fun returnsPrefixMatchesOnly() {
        val results = engine.suggest("key")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.startsWith("key", ignoreCase = true) })
    }

    @Test fun blankPrefixHasUsefulDefaults() {
        assertEquals(listOf("I", "the", "and"), engine.suggest(""))
    }
}
