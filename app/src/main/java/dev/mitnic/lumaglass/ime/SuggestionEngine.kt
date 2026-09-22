package dev.mitnic.lumaglass.ime

import java.util.Locale

class SuggestionEngine {
    private val common = listOf(
        "the", "to", "and", "a", "of", "is", "in", "that", "you", "it", "for", "on", "with",
        "this", "was", "are", "be", "have", "not", "at", "or", "from", "by", "but", "we", "they",
        "can", "will", "just", "what", "when", "how", "there", "your", "my", "me", "yes", "no",
        "hello", "hey", "thanks", "please", "okay", "good", "great", "today", "tomorrow", "because",
        "computer", "engineering", "project", "keyboard", "android", "college", "class", "work", "time",
        "I", "I'm", "I'll", "I've", "don't", "can't", "it's", "you're", "that's", "we're"
    )

    fun suggest(prefix: String, limit: Int = 3): List<String> {
        val clean = prefix.lowercase(Locale.ROOT).filter { it.isLetter() || it == '\'' }
        if (clean.isBlank()) return listOf("I", "the", "and").take(limit)
        return common.asSequence()
            .filter { it.lowercase(Locale.ROOT).startsWith(clean) && !it.equals(prefix, true) }
            .sortedWith(compareBy<String> { it.length }.thenBy { it })
            .take(limit)
            .toList()
    }

    fun currentWord(textBeforeCursor: CharSequence?): String = textBeforeCursor
        ?.takeLastWhile { it.isLetter() || it == '\'' }
        ?.toString()
        .orEmpty()
}
