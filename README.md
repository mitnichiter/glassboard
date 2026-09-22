# LumaGlass Keyboard

LumaGlass is a privacy-first Android input method with an Apple-inspired liquid-glass visual system. It is a real `InputMethodService`, not a keyboard mock-up.

## Current v0.1 features

- QWERTY, numbers, two symbol layers, shift, caps lock, delete repeat, space, punctuation and editor-aware enter keys
- Local prefix suggestions, suppressed in passwords and Incognito mode
- Emoji and Android clipboard panels
- Android system speech recognition (on-device or cloud-backed according to the selected system service)
- Haptics, optional key sounds, number row and adjustable key height
- Left/right one-handed layouts
- Aurora, Midnight and Pearl glass palettes
- System keyboard onboarding and settings app
- No `INTERNET` permission
- Haze 2 GPU-adaptive blur/refraction with a layered native Compose fallback

## Build and install

1. Open this folder in a current Android Studio installation with Android SDK 37.
2. Let Android Studio use Gradle 8.13 or generate a Gradle wrapper.
3. Build and run the `app` configuration on Android 8.0 or newer.
4. Open LumaGlass, tap **Open keyboard settings**, enable it, then tap **Choose LumaGlass**.
5. Grant microphone access only if you want voice typing.

The strongest optical effects require modern GPU/runtime-shader support. Haze automatically degrades to a simpler translucent material on unsupported devices.

## Architecture

- `ime/LumaGlassImeService.kt` — Android IME entry point and Compose lifecycle bridge
- `ime/KeyboardController.kt` — input connection, editor actions, clipboard, voice and feedback
- `ime/KeyboardModels.kt` — layouts and key actions
- `ime/SuggestionEngine.kt` — offline suggestion core
- `ui/GlassSurface.kt` — Haze glass stage and spring-loaded liquid keys
- `ui/KeyboardRoot.kt` — keyboard, suggestion strip, emoji and clipboard panels
- `MainActivity.kt` — onboarding, permissions and preferences

## Important platform limit

An Android IME is a separate window. It cannot capture pixels from the host app behind it, and it should not try: that boundary protects other apps' content. LumaGlass refracts a dynamic in-keyboard backdrop, producing the optical depth without crossing that privacy boundary.

## Before publishing

Change the application ID, add adaptive launcher artwork, test physical devices across Android 8–17, add accessibility labels/localization, ship a larger licensed dictionary, and complete Play Store privacy/data-safety declarations. See `ROADMAP.md`.

## License

Project source: Apache License 2.0. Haze is also Apache-2.0; retain its notices when redistributing.
