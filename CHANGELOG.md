# Changelog

## 1.0.0 - Production readiness

- Set Android version metadata to `versionName 1.0.0` and `versionCode 10000`.
- Added Android 12+ data extraction rules to keep local garden data out of cloud backup and device transfer.
- Hardened weather parsing so malformed Open-Meteo responses return failures instead of crashing.
- Added Room indexes for plant stages, tasks, care history, and plant photos.
- Added photo deletion support in the growth album and kept the plant card photo in sync.
- Replaced the settings-facing DeepSeek status with a generic AI detector status.
- Replaced the fixed weather advice text with condition-aware guidance.
- Reduced release logging for AI parsing and app integrity checks.
- Expanded GitHub Actions to run tests, lint, debug APK, release APK, and release AAB builds.

## Known release notes

- Google Play upload requires a real release keystore or Play App Signing setup. Local builds fall back to debug signing when `keystore.properties` is absent.
- Play Integrity token verification is client-side prepared only. Full anti-tamper enforcement still requires a backend verifier.
