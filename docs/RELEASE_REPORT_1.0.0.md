# GardenFlow 1.0.0 Release Report

## Build Result

- Version name: `1.0.0`
- Version code: `10000`
- Package: `com.tony.gardenflow`
- Min SDK: `29`
- Target SDK: `35`
- Compile SDK: `35`
- Local command: `./gradlew clean test lint assembleRelease bundleRelease`
- Result: `BUILD SUCCESSFUL`

## Artifacts

- APK: `outputs/GardenFlow-1.0.0-release.apk`
- AAB: `outputs/GardenFlow-1.0.0-release.aab`
- SHA-256: `outputs/GardenFlow-1.0.0-release.sha256`

## Audit Summary

- Architecture: Compose UI continues to call ViewModels; repositories own Room/service coordination; weather, AI, OCR, location, notifications, and work scheduling remain isolated services.
- Stability: Open-Meteo parsing now validates missing fields through `Result` instead of unsafe null assertions.
- Data: Room version moved to `7`; added indexes for common plant, task, history, and photo lookups.
- Privacy: Android backup/device-transfer extraction is disabled for app files, preferences, and databases.
- Security: HTTPS-only network policy remains enabled; FileProvider is non-exported; camera feature is optional; release logging no longer includes AI response snippets or Play Integrity token fragments.
- Secrets: Release builds intentionally leave AI endpoint, model, and API key blank. Debug builds may read `local.properties` for development, but API keys must not ship in Google Play artifacts.
- UX fixes: photo records can be deleted; AI status is shown as a generic detector; weather advice no longer uses a fixed outdoor-care phrase.
- CI: GitHub Actions now builds debug APK, release APK, release AAB, and uploads all artifacts.

## Google Play Readiness

Ready for internal QA and Play Console pre-upload validation once release signing is configured.

Not yet final-store-ready until:

- A production keystore or Play App Signing upload key is configured in `keystore.properties`.
- Play Integrity verdicts are verified server-side if anti-tamper enforcement is required.
- The Play Console Data Safety form is completed using the privacy checklist below.

## Privacy Checklist

- Camera: used only for seed packet OCR and plant growth photos.
- Photos/media: accessed through Android picker/camera flows; no broad storage permission is requested.
- Location: used only for garden weather; no background location permission is requested.
- Notifications: used only for due garden tasks and snoozed reminders.
- Network: Open-Meteo weather/geocoding, DeepSeek-compatible AI, optional plant research.
- Analytics/ads: no analytics SDK, ads SDK, advertising ID, account system, or tracking.
- Local data: plants, care history, tasks, settings, and photo URIs are stored locally in Room.

## Residual Risks

- Release artifacts built on this machine are debug-signed unless `keystore.properties` exists.
- AI generation is disabled in unsigned/local release artifacts until a safe production AI proxy or runtime configuration strategy is provided.
- Automated UI/device tests are not present yet; validation is unit tests + lint + release build.
- Target SDK 35 is current for near-term Play submission, but Google Play deadlines will require SDK 36 for submissions after the 2026 policy cutoff.
