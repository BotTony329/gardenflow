# GardenFlow Privacy Data Flow

Last reviewed: 2026-07-27

This file records the privacy facts verified from the Android source code before publishing the public Privacy Policy page.

## Local Storage

- Room database name: `gardenflow.db`.
- Stored locally: plants, growth stages, care history, generated tasks, settings, growth photo URI references, plant photo URI, seed packet image URI, and raw OCR/package text when saved.
- Android backup and device-transfer extraction are disabled for app files, shared preferences, and databases in `app/src/main/res/xml/data_extraction_rules.xml`.
- `android:allowBackup="false"` is set in `AndroidManifest.xml`.

## Permissions

- `INTERNET`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`
- `CAMERA`
- `POST_NOTIFICATIONS`

The manifest does not request background location, contacts, calendar, microphone, SMS, call log, storage-all-files, or advertising ID permissions.

## Network Destinations

- Configured DeepSeek-compatible endpoint from `BuildConfig.DEEPSEEK_API_URL`.
- `https://api.open-meteo.com/v1/forecast`
- `https://geocoding-api.open-meteo.com/v1/search`
- `https://en.wikipedia.org/w/rest.php/v1/search/page`
- `https://en.wikipedia.org/api/rest_v1/page/summary`

Cleartext traffic is disabled by `network_security_config.xml` and `AndroidManifest.xml`.

## AI Inputs

When the user generates a care plan, GardenFlow may send:

- Plant name or plant query.
- Sowing date.
- User-selected plant start state.
- Seed packet text or OCR text, compacted before request.
- Wikipedia plant research summary if available.

Release builds currently inject blank DeepSeek endpoint, key, and model unless a production configuration strategy is added.

## Photos And OCR

- Packet photos and growth photos can be captured by camera or selected through Android photo/document pickers.
- GardenFlow stores photo URI references.
- Google ML Kit Text Recognition is used for OCR.
- Deleting a GardenFlow photo record removes the app's local reference. It may not delete the original image from another gallery or document provider.

## Notifications

Notifications may display plant names, task counts, task labels, and weather/task reasons.

## Not Found In Current Codebase

- No account system.
- No ads SDK.
- No analytics SDK.
- No crash reporting SDK.
- No cloud database owned by GardenFlow.
- No contacts/calendar/microphone/SMS/call-log/payment collection.
