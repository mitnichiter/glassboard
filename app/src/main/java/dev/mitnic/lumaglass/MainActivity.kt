package dev.mitnic.lumaglass

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mitnic.lumaglass.data.GlassTheme
import dev.mitnic.lumaglass.data.KeyboardPreferences
import dev.mitnic.lumaglass.data.KeyboardPrefs
import dev.mitnic.lumaglass.data.OneHandedMode
import dev.mitnic.lumaglass.ui.LumaGlassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = KeyboardPreferences(this)
        setContent {
            var prefs by remember { mutableStateOf(store.load()) }
            fun update(value: KeyboardPrefs) {
                prefs = value
                store.save(value)
            }
            LumaGlassTheme(prefs.theme) {
                SetupScreen(
                    prefs = prefs,
                    onPrefs = ::update,
                    onEnable = { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                    onSwitch = { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() },
                )
            }
        }
    }
}

@Composable
private fun SetupScreen(
    prefs: KeyboardPrefs,
    onPrefs: (KeyboardPrefs) -> Unit,
    onEnable: () -> Unit,
    onSwitch: () -> Unit,
) {
    val microphone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val palette = dev.mitnic.lumaglass.ui.LocalGlassPalette.current
    Column(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(palette.deep, palette.mid, palette.deep)))
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Text("LumaGlass", color = palette.text, fontSize = 38.sp, fontWeight = FontWeight.Bold)
        Text(
            "A private Android keyboard shaped from light, depth and motion.",
            color = palette.text.copy(alpha = .70f), fontSize = 16.sp,
        )

        GlassCard {
            Text("1  Enable the keyboard", color = palette.text, fontWeight = FontWeight.SemiBold)
            Text("Android requires you to approve every system keyboard.", color = palette.text.copy(alpha = .68f), fontSize = 13.sp)
            Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("Open keyboard settings") }
            Text("2  Make it active", color = palette.text, fontWeight = FontWeight.SemiBold)
            Button(onClick = onSwitch, modifier = Modifier.fillMaxWidth()) { Text("Choose LumaGlass") }
        }

        Text("Appearance", color = palette.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        GlassCard {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                GlassTheme.entries.forEach { value ->
                    FilterChip(
                        selected = prefs.theme == value,
                        onClick = { onPrefs(prefs.copy(theme = value)) },
                        label = { Text(value.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
            Text("Key height  ${prefs.keyHeight} dp", color = palette.text)
            Slider(
                value = prefs.keyHeight.toFloat(),
                onValueChange = { onPrefs(prefs.copy(keyHeight = it.toInt())) },
                valueRange = 42f..58f,
                steps = 7,
            )
            Text("One-handed mode", color = palette.text)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OneHandedMode.entries.forEach { value ->
                    FilterChip(
                        selected = prefs.oneHanded == value,
                        onClick = { onPrefs(prefs.copy(oneHanded = value)) },
                        label = { Text(value.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
        }

        Text("Typing", color = palette.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        GlassCard {
            SettingSwitch("Suggestions", "Local-only next-word and prefix suggestions", prefs.suggestions) {
                onPrefs(prefs.copy(suggestions = it))
            }
            SettingSwitch("Number row", "Keep 0–9 above QWERTY", prefs.numberRow) {
                onPrefs(prefs.copy(numberRow = it))
            }
            SettingSwitch("Haptic feedback", "A subtle tap for each key", prefs.haptics) {
                onPrefs(prefs.copy(haptics = it))
            }
            SettingSwitch("Key sounds", "Quiet system key clicks", prefs.sound) {
                onPrefs(prefs.copy(sound = it))
            }
            SettingSwitch("Incognito", "Disable suggestions and personal learning", prefs.incognito) {
                onPrefs(prefs.copy(incognito = it))
            }
            Button(
                onClick = { microphone.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Allow voice typing") }
        }

        GlassCard {
            Text("Privacy", color = palette.text, fontWeight = FontWeight.Bold)
            Text(
                "Typed text and suggestions stay inside the keyboard. LumaGlass declares no internet permission, hides suggestions in password fields, and reads only Android's current clipboard item when you open the clipboard panel. Voice typing is handled by your selected Android speech service and may follow that service's own cloud/privacy settings.",
                color = palette.text.copy(alpha = .72f), fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = dev.mitnic.lumaglass.ui.LocalGlassPalette.current
    Column(
        Modifier.fillMaxWidth()
            .background(Color.White.copy(alpha = .10f), RoundedCornerShape(22.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val color = dev.mitnic.lumaglass.ui.LocalGlassPalette.current.text
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = color, fontWeight = FontWeight.Medium)
            Text(subtitle, color = color.copy(alpha = .60f), fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
