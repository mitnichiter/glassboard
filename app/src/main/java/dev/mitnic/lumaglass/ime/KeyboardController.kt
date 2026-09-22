package dev.mitnic.lumaglass.ime

import android.Manifest
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import dev.mitnic.lumaglass.MainActivity
import dev.mitnic.lumaglass.data.KeyboardPreferences
import dev.mitnic.lumaglass.data.KeyboardPrefs
import java.util.Locale

class KeyboardController(
    private val service: LumaGlassImeService,
    private val preferences: KeyboardPreferences,
) {
    private val suggestions = SuggestionEngine()
    private val clipboard = service.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var editorInfo: EditorInfo? = null
    private var lastShiftTap = 0L
    private var speechRecognizer: SpeechRecognizer? = null

    var state by mutableStateOf(KeyboardUiState(prefs = preferences.load()))
        private set

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener { refreshClipboard() }

    fun start() {
        clipboard.addPrimaryClipChangedListener(clipboardListener)
        refreshClipboard()
    }

    fun stop() {
        clipboard.removePrimaryClipChangedListener(clipboardListener)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun onStartInput(info: EditorInfo?) {
        editorInfo = info
        val inputType = info?.inputType ?: 0
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val password =
            (inputClass == InputType.TYPE_CLASS_TEXT && variation in setOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            )) ||
            (inputClass == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        val sentenceCaps = inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0
        state = state.copy(
            page = if (inputType and InputType.TYPE_CLASS_NUMBER != 0) KeyboardPage.SYMBOLS else KeyboardPage.LETTERS,
            shift = if (sentenceCaps) ShiftState.ONCE else ShiftState.OFF,
            isPassword = password,
            suggestions = emptyList(),
            enterLabel = enterLabel(info?.imeOptions ?: 0),
            prefs = preferences.load(),
        )
        updateSuggestions()
    }

    fun handle(action: KeyAction) {
        feedback()
        when (action) {
            is KeyAction.Text -> commit(action.value)
            KeyAction.Shift -> cycleShift()
            KeyAction.Delete -> delete(feedback = false)
            KeyAction.Space -> commit(" ")
            KeyAction.Enter -> enter()
            KeyAction.Symbols -> state = state.copy(page = KeyboardPage.SYMBOLS)
            KeyAction.Letters -> state = state.copy(page = KeyboardPage.LETTERS)
            KeyAction.MoreSymbols -> state = state.copy(page = KeyboardPage.SYMBOLS_MORE)
            KeyAction.Emoji -> state = state.copy(page = KeyboardPage.EMOJI)
            KeyAction.Clipboard -> state = state.copy(page = KeyboardPage.CLIPBOARD)
            KeyAction.Voice -> toggleVoice()
            KeyAction.NextLanguage -> service.switchToNextInputMethod(false)
            KeyAction.Settings -> openSettings()
        }
    }

    fun repeatDelete() = delete(feedback = false)

    fun chooseSuggestion(word: String) {
        val connection = service.currentInputConnection ?: return
        val current = suggestions.currentWord(connection.getTextBeforeCursor(64, 0))
        if (current.isNotEmpty()) connection.deleteSurroundingText(current.length, 0)
        connection.commitText("$word ", 1)
        state = state.copy(shift = ShiftState.OFF)
        updateSuggestions()
    }

    fun moveCursor(delta: Int) {
        if (delta == 0) return
        val connection = service.currentInputConnection ?: return
        val before = connection.getTextBeforeCursor(2048, 0)?.length ?: return
        val after = connection.getTextAfterCursor(2048, 0)?.length ?: 0
        val position = (before + delta).coerceIn(0, before + after)
        connection.setSelection(position, position)
    }

    fun updatePrefs(transform: (KeyboardPrefs) -> KeyboardPrefs) {
        val updated = transform(state.prefs)
        preferences.save(updated)
        state = state.copy(prefs = updated)
    }

    private fun commit(raw: String) {
        val text = if (state.shift != ShiftState.OFF && raw.length == 1 && raw[0].isLetter()) {
            raw.uppercase(Locale.getDefault())
        } else raw
        service.currentInputConnection?.commitText(text, 1)
        if (state.shift == ShiftState.ONCE && text.any(Char::isLetter)) {
            state = state.copy(shift = ShiftState.OFF)
        }
        updateSuggestions()
    }

    private fun delete(feedback: Boolean = true) {
        if (feedback) feedback()
        val connection = service.currentInputConnection ?: return
        val selected = connection.getSelectedText(0)
        if (!selected.isNullOrEmpty()) connection.commitText("", 1)
        else connection.deleteSurroundingTextInCodePoints(1, 0)
        updateSuggestions()
    }

    private fun cycleShift() {
        val now = SystemClock.uptimeMillis()
        val next = when {
            state.shift == ShiftState.CAPS -> ShiftState.OFF
            state.shift == ShiftState.ONCE && now - lastShiftTap < 350 -> ShiftState.CAPS
            state.shift == ShiftState.OFF -> ShiftState.ONCE
            else -> ShiftState.OFF
        }
        lastShiftTap = now
        state = state.copy(shift = next)
    }

    private fun enter() {
        val action = (editorInfo?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            service.currentInputConnection?.performEditorAction(action)
        } else {
            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    private fun updateSuggestions() {
        if (state.isPassword || state.prefs.incognito || !state.prefs.suggestions) {
            state = state.copy(suggestions = emptyList())
            return
        }
        val before = service.currentInputConnection?.getTextBeforeCursor(64, 0)
        state = state.copy(suggestions = suggestions.suggest(suggestions.currentWord(before)))
    }

    private fun refreshClipboard() {
        val clip = clipboard.primaryClip
        val items = if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            (0 until minOf(clip?.itemCount ?: 0, 6)).mapNotNull { index ->
                clip?.getItemAt(index)?.coerceToText(service)?.toString()?.take(160)
            }
        } else emptyList()
        state = state.copy(clipboardItems = items)
    }

    private fun feedback() {
        if (state.prefs.haptics) {
            service.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (state.prefs.sound) {
            (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                .playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.35f)
        }
    }

    private fun toggleVoice() {
        if (state.isListening) {
            speechRecognizer?.stopListening()
            return
        }
        if (ContextCompat.checkSelfPermission(service, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(service, "Open LumaGlass settings and allow microphone access", Toast.LENGTH_LONG).show()
            openSettings()
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(service)) {
            Toast.makeText(service, "Speech recognition is unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(service).also {
            speechRecognizer = it
            it.setRecognitionListener(VoiceListener())
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        state = state.copy(isListening = true)
        recognizer.startListening(intent)
    }

    private fun openSettings() {
        service.startActivity(Intent(service, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun enterLabel(options: Int): String = when (options and EditorInfo.IME_MASK_ACTION) {
        EditorInfo.IME_ACTION_GO -> "go"
        EditorInfo.IME_ACTION_SEARCH -> "⌕"
        EditorInfo.IME_ACTION_SEND -> "send"
        EditorInfo.IME_ACTION_NEXT -> "next"
        EditorInfo.IME_ACTION_DONE -> "done"
        else -> "↵"
    }

    private inner class VoiceListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() { state = state.copy(isListening = false) }
        override fun onError(error: Int) { state = state.copy(isListening = false) }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
        override fun onPartialResults(partialResults: Bundle?) = Unit
        override fun onResults(results: Bundle?) {
            state = state.copy(isListening = false)
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.let { service.currentInputConnection?.commitText("$it ", 1) }
            updateSuggestions()
        }
    }
}
