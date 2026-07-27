# GardenFlow v1.0 Pre-Launch Validation

Validation date: 2026-07-27  
Validator: Codex local QA pass  
Project path: `/Users/tz/Documents/Codex/2026-07-23/codex-prd-codex-apk-codex-prompt`

## Final Recommendation

**NOT READY FOR RELEASE**

GardenFlow now builds cleanly and produces release APK/AAB artifacts, but it is not ready for Google Play production submission yet because the locally generated release artifacts are signed with the Android debug certificate, no connected device/emulator was available for install and runtime validation, and the production privacy-policy/store-console items still need to be completed by the developer.

It is signing-ready for a real Play upload key once `keystore.properties` or CI signing secrets are configured.

## A. Pre-Launch Audit Report

### Baseline

| Item | Value |
| --- | --- |
| Application ID | `com.tony.gardenflow` |
| App name | `GardenFlow` |
| Version name | `1.0.0` |
| Version code | `10000` |
| Min SDK | 29 |
| Target SDK | 35 |
| Compile SDK | 35 |
| Gradle | 8.10.2 |
| Android Gradle Plugin | 8.8.0 |
| Kotlin | 1.9.24 |
| JDK | 17.0.19 |
| Compose BOM | 2024.12.01 |
| Room | 2.6.1 |
| Hilt | 2.52 |
| WorkManager | 2.10.0 |
| CameraX | 1.4.1 |
| ML Kit Text Recognition | 16.0.1 |
| Network libraries | OkHttp 4.12.0; Open-Meteo HTTP endpoints |
| Play Integrity | 1.6.0 |
| Runtime permissions | Internet, coarse/fine location, camera, notifications; WorkManager adds wake lock, network state, boot completed, foreground service |
| Release APK size | 47 MB |
| Release AAB size | 24 MB |
| Mapping file size | 50 MB |

### Build Evidence

Command executed:

```bash
./gradlew clean lint test assembleDebug assembleRelease bundleRelease
```

Result:

```text
BUILD SUCCESSFUL in 6m 1s
136 actionable tasks: 133 executed, 3 up-to-date
```

Generated artifacts:

| Artifact | Path | Size |
| --- | --- | --- |
| Release APK | `outputs/prelaunch/GardenFlow-1.0.0-rc-release.apk` | 47 MB |
| Release AAB | `outputs/prelaunch/GardenFlow-1.0.0-rc-release.aab` | 24 MB |
| R8 mapping | `outputs/prelaunch/GardenFlow-1.0.0-rc-mapping.txt` | 50 MB |
| SHA-256 file | `outputs/prelaunch/GardenFlow-1.0.0-rc.sha256` | 348 B |

Checksums:

```text
0f4f106f4917710b19048b1494db4abc78b003bdc0fdb5d651625ebd82b818b5  GardenFlow-1.0.0-rc-release.apk
561e60c6227099d790a93aed0e606933db6b520b5408b60d36f815ed87b8cbe0  GardenFlow-1.0.0-rc-release.aab
75ca955e5eb14359de83e657e6e2d3f095c038d0f290023caf58089bd4c4d5a1  GardenFlow-1.0.0-rc-mapping.txt
```

### Issues Found And Fixes

| Severity | Issue | Root cause | Fix | Retest evidence |
| --- | --- | --- | --- | --- |
| Critical | DeepSeek API key was present in release APK during initial scan | Release `BuildConfig` inherited values from `local.properties` | Release build now injects blank AI endpoint/model/key; debug keeps local development values | Exact key/prefix scan of rebuilt APK and AAB returned no matches |
| High | Release artifact is debug-signed | No production `keystore.properties` exists locally | Added secure manual release workflow using GitHub Secrets; local build remains signing-ready | `apksigner` shows `CN=Android Debug`, so production remains blocked |
| Medium | Connected install/runtime flows were not executable locally | No device/emulator connected | Documented manual Samsung test script | `adb devices` returned no devices |
| Low | Kotlin deprecation warnings | Status/nav bar API and old ArrowBack icon alias | Documented as non-blocking technical debt | Build/lint passed |

## B. Test Matrix

| ID | Feature | Scenario | Environment | Expected | Actual | Status |
| --- | --- | --- | --- | --- | --- | --- |
| T01 | Build | Clean build | macOS local, JDK 17, Android SDK 35 | Success | `BUILD SUCCESSFUL` | Pass |
| T02 | Unit tests | JVM test suite | Local Gradle | All pass | `:app:test` passed | Pass |
| T03 | Lint | Android lint | Local Gradle | No release blocker | `:app:lint` passed | Pass |
| T04 | Debug APK | `assembleDebug` | Local Gradle | APK generated | `app-debug.apk`, 76 MB | Pass |
| T05 | Release APK | `assembleRelease` | Local Gradle | APK generated | release APK, 47 MB | Pass |
| T06 | Release AAB | `bundleRelease` | Local Gradle | AAB generated | release AAB, 24 MB | Pass |
| T07 | Package identity | `aapt dump badging` | Release APK | Correct ID/version/SDK | `com.tony.gardenflow`, `1.0.0`, target 35 | Pass |
| T08 | Permissions | `aapt dump permissions` | Release APK | Expected permissions only | Expected app permissions plus WorkManager transitive permissions | Pass with note |
| T09 | Secrets | Exact strings scan | Release APK/AAB | No API key/private key | No DeepSeek key, no `local.properties`, no private key match | Pass |
| T10 | Signing | `apksigner verify` | Release APK | Valid release/upload key | Valid APK signature, but Android debug cert | Fail for production |
| T11 | AAB signing | `jarsigner -verify` | Release AAB | Release/upload key | Android debug cert | Fail for production |
| T12 | Device runtime | `adb devices` | Local machine | Device available | No devices attached | Not run |
| T13 | Fresh install journey | Manual | Real Samsung phone | Complete first-user flow | Not executed locally | Blocked |
| T14 | Camera/OCR runtime | Manual | Real device | Camera/photo picker/OCR works | Not executed locally | Blocked |
| T15 | Notifications | Manual | Real Android 13+ device | Notification permission/channels work | Not executed locally | Blocked |
| T16 | Location runtime | Manual | Real device | Permission and city fallback work | Not executed locally | Blocked |

## C. Security Report

### Attack Surface

- Exported launcher activity: `MainActivity`.
- Non-exported `FileProvider` for camera/photo URIs.
- WorkManager services/receivers, including system job service protected by `BIND_JOB_SERVICE`.
- ML Kit provider/services.
- Network calls to Open-Meteo and optional AI endpoint.
- Local Room database and shared settings.
- Photo picker/camera input and OCR text.

### Findings

| Severity | Finding | Status |
| --- | --- | --- |
| Critical | Release package previously contained a real DeepSeek key | Fixed and retested |
| High | Current release artifacts are debug-signed | Open blocker |
| Medium | AI configuration in production release is blank until a safe production configuration/proxy is chosen | Open operational decision |
| Medium | Real-device permission abuse/denial paths not validated in this environment | Open manual QA |
| Low | Native ML Kit assets make package large | Accepted for OCR support |

### Security Configuration Verified

- `android:allowBackup="false"`.
- `android:dataExtractionRules="@xml/data_extraction_rules"`.
- `android:usesCleartextTraffic="false"`.
- Network security config exists.
- `local.properties`, `keystore.properties`, `*.jks`, and `*.keystore` are ignored by `.gitignore`.
- Release package exact secret scan no longer finds the DeepSeek key.

### Residual Risks

- No production upload key was used in this local artifact.
- If AI is enabled directly in a client build later, API keys can be extracted from the APK/AAB. Prefer a backend proxy for production.
- Play Integrity is included but final enforcement depends on production certificate SHA and server-side validation strategy.

## D. Privacy And Data Safety Draft

### Data Flow

| Data | Source | Purpose | Stored | Transmitted | Retention/Delete |
| --- | --- | --- | --- | --- | --- |
| Plant name/variety | User/OCR/AI | Garden records and reminders | Room DB | Optional AI prompt | Deleted when plant data is cleared/deleted |
| Sowing date/status | User | Growth stage and care schedule | Room DB | Optional AI prompt | Deleted with plant |
| Care history | User actions | Task scheduling/history | Room DB | No intended third-party sharing | Deleted with plant/clear data |
| Garden location/coordinates | User/manual/location API | Weather forecast | Local settings | Open-Meteo request | Deleted/changed in settings |
| Seed-packet image | Camera/photo picker | OCR | URI may be retained only when app stores image reference | ML Kit local processing; OCR text may be sent to AI | User can remove/replace depending on screen |
| OCR text | ML Kit | Generate plan | Raw text may be stored with plant | Optional AI prompt | Deleted with plant |
| AI response | DeepSeek-compatible service | Plant care plan | Room DB structured fields | Returned from AI service | Deleted with plant |
| Weather data | Open-Meteo | Weather-aware task rules | Cached/settings-derived app state | Open-Meteo receives coordinates | Refreshed/overwritten |
| Notifications | App-generated | Reminders | Android notification system | No third-party app server | Dismissed by user/system |

### Draft Play Data Safety Answers

- Data collection by developer: local plant/care/location data is processed by the app; third-party transmission may occur for AI and weather.
- Data shared with third parties: weather coordinates to Open-Meteo; plant/package text to configured AI provider when AI generation is used.
- Location: approximate/precise location may be used for weather if permission granted; manual city/coordinates are supported.
- Photos/videos: camera/photo picker used for seed packet OCR and plant growth photos; broad storage permissions are not used.
- Account data: none.
- Ads: none.
- In-app purchases/subscriptions: none.
- Children: do not declare as designed for children unless a separate compliance review is completed.

Manual verification required: privacy policy URL, support contact, exact third-party processor wording, whether production AI uses direct DeepSeek or a backend proxy.

## E. Google Play Submission Checklist

- [ ] Create/choose production Play app listing.
- [ ] Configure Google Play App Signing.
- [ ] Generate an upload key and keep it backed up.
- [ ] Add GitHub Secrets: `ANDROID_UPLOAD_KEYSTORE_BASE64`, `ANDROID_UPLOAD_KEYSTORE_PASSWORD`, `ANDROID_UPLOAD_KEY_ALIAS`, `ANDROID_UPLOAD_KEY_PASSWORD`.
- [ ] Build release AAB with `android-release.yml` or local `keystore.properties`.
- [ ] Verify AAB is not debug-signed.
- [ ] Upload AAB to Internal Testing first.
- [ ] Add tester emails/groups.
- [ ] Complete Data Safety form matching the privacy draft above.
- [ ] Add privacy policy URL.
- [ ] Complete content rating questionnaire.
- [ ] Complete app access declaration.
- [ ] Declare no ads if unchanged.
- [ ] Review location, photo/video, notification, and AI-related policy declarations.
- [ ] Run Google Play Pre-launch Report and triage every crash/ANR/visual/accessibility finding.
- [ ] Only promote after internal testing proves install, update, camera, OCR, AI, weather, reminders, and notifications on real devices.

Policy references checked on 2026-07-27:

- Google Play target API policy: https://support.google.com/googleplay/android-developer/answer/11926878
- Play Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Photo and video permissions policy: https://support.google.com/googleplay/android-developer/answer/14115180
- Android Photo Picker: https://developer.android.com/training/data-storage/shared/photopicker
- Android backup and data extraction rules: https://developer.android.com/about/versions/12/backup-restore

Important policy note: target SDK 35 is acceptable for the current release candidate window, but Google Play target API requirements can advance. Recheck before submission, especially after 2026-08-31.

## F. Release Artifacts

Current artifacts are build outputs, not Play-production-signed outputs:

- `outputs/prelaunch/GardenFlow-1.0.0-rc-release.apk`
- `outputs/prelaunch/GardenFlow-1.0.0-rc-release.aab`
- `outputs/prelaunch/GardenFlow-1.0.0-rc-mapping.txt`
- `outputs/prelaunch/GardenFlow-1.0.0-rc.sha256`

Native debug symbols: no separate native debug symbol package was generated. Native library strip warnings came from bundled ML Kit/AndroidX native libraries and did not fail the build.

Release notes draft:

```text
GardenFlow 1.0.0
- Weather-aware garden reminders
- Manual and seed-packet-based plant creation
- Local care history and growth tracking
- Camera/photo picker OCR support
- English and Chinese interface support
```

## G. Known Limitations

- Local release artifacts are debug-signed until a production upload key is configured.
- Production release has blank AI endpoint/key/model by design to avoid shipping secrets; production AI should use a backend proxy or secure runtime configuration.
- No connected device/emulator was available during this validation, so install/runtime flows require manual testing.
- Accessibility with TalkBack, 200% font scale, landscape, tablet, and split-screen still needs real-device confirmation.
- WorkManager timing cannot be exact by Android design.
- ML Kit OCR increases app size.

## H. Manual Samsung Phone Test Script

- [ ] Uninstall old GardenFlow.
- [ ] Install the Play internal-testing version, not a debug APK.
- [ ] First launch the app.
- [ ] Deny notification permission once.
- [ ] Deny camera permission once.
- [ ] Deny location permission once.
- [ ] Enable location later from Settings or use manual city search.
- [ ] Create a plant manually.
- [ ] Create a plant by scanning a seed packet.
- [ ] Test AI generation with internet enabled.
- [ ] Disable internet and confirm AI/weather failures show readable fallback messages.
- [ ] Restore internet and regenerate a care plan.
- [ ] Tap Water on a plant and confirm history updates.
- [ ] Tap Fertilise and confirm history updates.
- [ ] Snooze a due reminder.
- [ ] Confirm notification delivery when a task is due.
- [ ] Restart the phone.
- [ ] Reopen GardenFlow and confirm plants/history/settings remain.
- [ ] Change system language between English and Chinese.
- [ ] Increase font size and verify no clipped primary controls.
- [ ] Enable dark mode and verify readability.
- [ ] Update from the previous installed version and confirm no data loss.
- [ ] Inspect Android app permissions in system settings.
- [ ] Verify privacy policy link in the Play listing.
- [ ] Verify version shown as 1.0.0.
- [ ] Record screenshots/logs for every unexpected result.
