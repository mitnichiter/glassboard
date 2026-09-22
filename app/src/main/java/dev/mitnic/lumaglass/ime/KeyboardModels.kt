package dev.mitnic.lumaglass.ime

import dev.mitnic.lumaglass.data.KeyboardPrefs

enum class KeyboardPage { LETTERS, SYMBOLS, SYMBOLS_MORE, EMOJI, CLIPBOARD }
enum class ShiftState { OFF, ONCE, CAPS }

sealed interface KeyAction {
    data class Text(val value: String) : KeyAction
    data object Shift : KeyAction
    data object Delete : KeyAction
    data object Space : KeyAction
    data object Enter : KeyAction
    data object Symbols : KeyAction
    data object Letters : KeyAction
    data object MoreSymbols : KeyAction
    data object Emoji : KeyAction
    data object Clipboard : KeyAction
    data object Voice : KeyAction
    data object NextLanguage : KeyAction
    data object Settings : KeyAction
}

data class KeySpec(
    val label: String,
    val action: KeyAction,
    val weight: Float = 1f,
    val alternate: String? = null,
    val emphasized: Boolean = false,
)

data class KeyboardUiState(
    val page: KeyboardPage = KeyboardPage.LETTERS,
    val shift: ShiftState = ShiftState.ONCE,
    val suggestions: List<String> = emptyList(),
    val prefs: KeyboardPrefs = KeyboardPrefs(),
    val isPassword: Boolean = false,
    val isListening: Boolean = false,
    val enterLabel: String = "↵",
    val clipboardItems: List<String> = emptyList(),
)

object KeyboardLayouts {
    val letters = listOf(
        "qwertyuiop".map { KeySpec(it.toString(), KeyAction.Text(it.toString())) },
        "asdfghjkl".map { KeySpec(it.toString(), KeyAction.Text(it.toString())) },
        listOf(KeySpec("⇧", KeyAction.Shift, 1.25f, emphasized = true)) +
            "zxcvbnm".map { KeySpec(it.toString(), KeyAction.Text(it.toString())) } +
            KeySpec("⌫", KeyAction.Delete, 1.25f, emphasized = true),
        listOf(
            KeySpec("123", KeyAction.Symbols, 1.25f),
            KeySpec("☺", KeyAction.Emoji, 1f),
            KeySpec(",", KeyAction.Text(","), 0.8f),
            KeySpec("space", KeyAction.Space, 4f),
            KeySpec(".", KeyAction.Text("."), 0.8f),
            KeySpec("↵", KeyAction.Enter, 1.25f, emphasized = true),
        ),
    )

    val symbols = listOf(
        "1234567890".map { KeySpec(it.toString(), KeyAction.Text(it.toString())) },
        listOf("@", "#", "₹", "_", "&", "-", "+", "(", ")", "/")
            .map { KeySpec(it, KeyAction.Text(it)) },
        listOf(
            KeySpec("=\\<", KeyAction.MoreSymbols, 1.25f),
            *listOf("*", "\"", "'", ":", ";", "!", "?").map { KeySpec(it, KeyAction.Text(it)) }.toTypedArray(),
            KeySpec("⌫", KeyAction.Delete, 1.25f, emphasized = true),
        ),
        listOf(
            KeySpec("ABC", KeyAction.Letters, 1.25f),
            KeySpec("☺", KeyAction.Emoji, 1f),
            KeySpec(",", KeyAction.Text(","), 0.8f),
            KeySpec("space", KeyAction.Space, 4f),
            KeySpec(".", KeyAction.Text("."), 0.8f),
            KeySpec("↵", KeyAction.Enter, 1.25f, emphasized = true),
        ),
    )

    val symbolsMore = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map { KeySpec(it, KeyAction.Text(it)) },
        listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\").map { KeySpec(it, KeyAction.Text(it)) },
        listOf(
            KeySpec("?123", KeyAction.Symbols, 1.25f),
            *listOf("%", "©", "®", "™", "✓", "[", "]").map { KeySpec(it, KeyAction.Text(it)) }.toTypedArray(),
            KeySpec("⌫", KeyAction.Delete, 1.25f, emphasized = true),
        ),
        symbols.last(),
    )
}
