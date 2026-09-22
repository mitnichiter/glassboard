package dev.mitnic.lumaglass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mitnic.lumaglass.data.OneHandedMode
import dev.mitnic.lumaglass.ime.KeyAction
import dev.mitnic.lumaglass.ime.KeySpec
import dev.mitnic.lumaglass.ime.KeyboardController
import dev.mitnic.lumaglass.ime.KeyboardLayouts
import dev.mitnic.lumaglass.ime.KeyboardPage
import dev.mitnic.lumaglass.ime.ShiftState

private val emojis = listOf(
    "😀","😃","😄","😁","😆","🥹","😂","🙂","🙃","😉","😊","🥰",
    "😍","🤩","😘","😋","😎","🤓","🫡","🤔","🫢","😭","😤","😡",
    "👍","👎","👏","🙌","🙏","💪","🔥","✨","❤️","💜","💙","💚",
    "🎉","✅","👀","💀","🚀","💯","🤝","🫶","😮","🥳","😴","🤯"
)

@Composable
fun KeyboardRoot(controller: KeyboardController) {
    val state = controller.state
    val align = when (state.prefs.oneHanded) {
        OneHandedMode.LEFT -> Alignment.CenterStart
        OneHandedMode.RIGHT -> Alignment.CenterEnd
        OneHandedMode.OFF -> Alignment.Center
    }
    val width = if (state.prefs.oneHanded == OneHandedMode.OFF) 1f else .82f
    LiquidGlassStage(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth(), contentAlignment = align) {
            Column(
                Modifier.fillMaxWidth(width).padding(horizontal = 5.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Toolbar(controller)
                when (state.page) {
                    KeyboardPage.EMOJI -> EmojiPanel(controller)
                    KeyboardPage.CLIPBOARD -> ClipboardPanel(controller)
                    else -> KeyboardRows(controller)
                }
            }
        }
    }
}

@Composable
private fun Toolbar(controller: KeyboardController) {
    val state = controller.state
    Row(
        Modifier.fillMaxWidth().height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        ToolbarButton("⌘", 1f) { controller.handle(KeyAction.Settings) }
        ToolbarButton("▣", 1f) { controller.handle(KeyAction.Clipboard) }
        if (!state.isPassword && state.suggestions.isNotEmpty()) {
            state.suggestions.forEach { word ->
                LiquidKeySurface(false, false, { controller.chooseSuggestion(word) }, Modifier.weight(1.6f).fillMaxHeight()) {
                    Text(word, Modifier.align(Alignment.Center), color = LocalGlassPalette.current.text, fontSize = 14.sp, maxLines = 1)
                }
            }
        } else {
            Spacer(Modifier.weight(3f))
        }
        ToolbarButton(if (state.isListening) "●" else "♩", 1f) { controller.handle(KeyAction.Voice) }
        ToolbarButton("◎", 1f) { controller.handle(KeyAction.NextLanguage) }
    }
}

@Composable
private fun ToolbarButton(label: String, weight: Float, onClick: () -> Unit) {
    LiquidKeySurface(false, false, onClick, Modifier.weight(weight).fillMaxHeight()) {
        Text(label, Modifier.align(Alignment.Center), color = LocalGlassPalette.current.text, fontSize = 17.sp)
    }
}

@Composable
private fun KeyboardRows(controller: KeyboardController) {
    val state = controller.state
    val rows = when (state.page) {
        KeyboardPage.SYMBOLS -> KeyboardLayouts.symbols
        KeyboardPage.SYMBOLS_MORE -> KeyboardLayouts.symbolsMore
        else -> KeyboardLayouts.letters
    }
    if (state.prefs.numberRow && state.page == KeyboardPage.LETTERS) {
        KeyRow("1234567890".map { KeySpec(it.toString(), KeyAction.Text(it.toString())) }, controller)
    }
    rows.forEach { original ->
        val row = original.map { key ->
            when {
                key.action == KeyAction.Enter -> key.copy(label = state.enterLabel)
                key.action == KeyAction.Shift && state.shift == ShiftState.CAPS -> key.copy(label = "⇧·")
                else -> key
            }
        }
        KeyRow(row, controller)
    }
}

@Composable
private fun KeyRow(keys: List<KeySpec>, controller: KeyboardController) {
    val state = controller.state
    Row(
        Modifier.fillMaxWidth().height(state.prefs.keyHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        keys.forEach { key ->
            val shown = if (
                key.action is KeyAction.Text && state.shift != ShiftState.OFF && key.label.length == 1
            ) key.label.uppercase() else key.label
            LiquidKeySurface(
                repeat = key.action == KeyAction.Delete,
                emphasized = key.emphasized || (key.action == KeyAction.Shift && state.shift != ShiftState.OFF),
                onClick = { controller.handle(key.action) },
                modifier = Modifier.weight(key.weight).fillMaxHeight(),
            ) {
                Text(
                    shown,
                    Modifier.align(Alignment.Center),
                    color = LocalGlassPalette.current.text,
                    fontSize = if (shown.length > 3) 13.sp else 20.sp,
                    fontWeight = if (key.emphasized) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EmojiPanel(controller: KeyboardController) {
    Column(Modifier.fillMaxWidth().height(212.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(emojis) { emoji ->
                LiquidKeySurface(false, false, { controller.handle(KeyAction.Text(emoji)) }, Modifier.height(38.dp)) {
                    Text(emoji, Modifier.align(Alignment.Center), fontSize = 22.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(45.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ToolbarButton("ABC", 1.2f) { controller.handle(KeyAction.Letters) }
            Spacer(Modifier.weight(3f))
            ToolbarButton("⌫", 1.2f) { controller.handle(KeyAction.Delete) }
        }
    }
}

@Composable
private fun ClipboardPanel(controller: KeyboardController) {
    val items = controller.state.clipboardItems
    Column(Modifier.fillMaxWidth().height(212.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (items.isEmpty()) {
            Text(
                "Clipboard is empty. Copied text appears here only while Android keeps it available.",
                Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                color = LocalGlassPalette.current.text.copy(alpha = .72f),
                textAlign = TextAlign.Center,
            )
        } else {
            Row(Modifier.weight(1f).fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items.forEach { clip ->
                    LiquidKeySurface(false, false, { controller.handle(KeyAction.Text(clip)) }, Modifier.width(160.dp).fillMaxHeight()) {
                        Text(clip, Modifier.padding(12.dp), color = LocalGlassPalette.current.text, maxLines = 5, fontSize = 13.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(45.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ToolbarButton("ABC", 1.2f) { controller.handle(KeyAction.Letters) }
            Spacer(Modifier.weight(3f))
        }
    }
}
