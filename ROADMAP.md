# Product roadmap

“All Gboard features” is a product-scale goal, not a credible single release. This plan keeps the keyboard usable at every stage.

## v0.2 — typing quality

- Trie/finite-state lexicon with frequency ranking
- Contextual next-word language model and opt-in local learning
- Autocorrect with undo chip and confidence thresholds
- Long-press accent popups and punctuation shortcuts
- Spacebar cursor control and swipe-to-delete
- Proper URL, email, phone and numeric editor layouts
- Accessibility semantics, TalkBack passes and reduced-transparency support

## v0.3 — glide and language packs

- Geometry-aware glide decoder with beam search
- Downloadable, signed language packs
- Malayalam, Manglish, Hindi and English multilingual switching
- Transliteration and bilingual suggestions
- Per-language dictionaries and layouts

## v0.4 — rich input

- GIF/sticker search through a separately disclosed network feature
- Searchable emoji, recent/favorites and skin tones
- Clipboard history with explicit opt-in, pinning, encryption and expiry
- Theme editor, wallpaper-derived color and per-app preferences
- Floating keyboard, resize handles, split tablet layout and handwriting pad

## v1.0 — production hardening

- Fuzzing for `InputConnection` edge cases and Unicode grapheme deletion
- Macrobenchmark latency, startup and GPU frame-time budgets
- Crash reporting only with explicit consent
- Threat model, privacy review and independent accessibility testing
- Play Store release pipeline, signing and reproducible builds

Cloud translation, Assistant/Gemini features and account sync must remain optional modules. They require explicit network/data disclosures and cannot be honestly described as private/offline.
