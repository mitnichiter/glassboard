package dev.mitnic.lumaglass.ime

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.mitnic.lumaglass.data.KeyboardPreferences
import dev.mitnic.lumaglass.ui.KeyboardRoot
import dev.mitnic.lumaglass.ui.LumaGlassTheme

class LumaGlassImeService : InputMethodService() {
    private lateinit var owners: ImeViewTreeOwners
    private lateinit var controller: KeyboardController

    override fun onCreate() {
        super.onCreate()
        owners = ImeViewTreeOwners().also { it.create() }
        controller = KeyboardController(this, KeyboardPreferences(this)).also { it.start() }
    }

    override fun onCreateInputView(): View = ComposeView(this).apply {
        setBackgroundColor(Color.TRANSPARENT)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(owners)
        setViewTreeSavedStateRegistryOwner(owners)
        setViewTreeViewModelStoreOwner(owners)
        setContent {
            LumaGlassTheme(controller.state.prefs.theme) {
                KeyboardRoot(controller)
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        controller.onStartInput(attribute)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        owners.resume()
    }

    override fun onWindowHidden() {
        owners.pause()
        super.onWindowHidden()
    }

    override fun onDestroy() {
        controller.stop()
        owners.destroy()
        super.onDestroy()
    }
}
