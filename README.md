# GardenFlow

GardenFlow is a native Android gardening todo app for personal home gardening. It turns plant identity, seed packet or nursery plant information, care cycles, local weather, and Android reminders into a local-first task system.

The product is not a plant encyclopedia and not a social gardening app. The core promise is:

```text
Scan or enter a plant
Answer what state the plant is in
Let AI create a care plan
Let weather and care history decide what needs attention today
```

This README is also a product and UI handoff document for designers working on the next visual upgrade.

## Product Positioning

GardenFlow is a:

```text
Weather-aware garden task app
```

The daily experience should feel closer to:

- Google Tasks
- Samsung Reminder
- Todoist
- A calm plant care dashboard

It should not feel like:

- A plant encyclopedia
- A farm ERP
- A social app
- A decorative landing page

The user opens GardenFlow to answer one question:

```text
What does my garden need now?
```

## Current Product Scope

Implemented:

- Add plants by name
- Add plants from seed packet or plant label image
- OCR using Google ML Kit
- AI-generated care plan using DeepSeek-compatible API
- User questionnaire for plant starting state
- Local Room database
- Weather-aware watering rules
- Manual quick record buttons for watering and fertilising
- Plant growth timeline
- User-confirmed plant stage
- Garden location settings
- Android notifications
- WorkManager background task checks
- English and Chinese UI text
- Bottom navigation: Garden / Settings
- Release hardening for Google Play preparation

Not yet fully implemented:

- Full custom CameraX camera screen
- Backend Play Integrity verification
- Cloud sync
- Account system
- Export data
- Rich editable care-plan editor

## Technology Stack

- Language: Kotlin
- UI: Jetpack Compose
- Design system: Material Design 3
- Architecture: MVVM + Repository + Service Layer
- Dependency injection: Hilt
- Local storage: Room
- Background work: WorkManager
- Notifications: Android Notification Channels
- OCR: Google ML Kit Text Recognition
- Image input: Android Photo Picker compatible image selection
- Weather: Open-Meteo
- AI: DeepSeek-compatible API
- HTTP: OkHttp
- JSON: Kotlin Serialization
- Security: Android Network Security Config, R8, Play Integrity client hook
- Minimum Android: Android 10 / API 29
- Target SDK: 35

## Architecture

```text
Compose UI
↓
ViewModel
↓
Repository
↓
Room / Services
↓
DeepSeek / Open-Meteo / ML Kit / Android system APIs
```

Important boundaries:

- UI only displays state and emits events.
- ViewModels coordinate user actions.
- Repositories own local persistence and task updates.
- Services own third-party APIs.
- `ReminderEngine` owns task generation rules.
- Weather data never creates tasks directly.
- AI creates care rules, not daily runtime decisions.

## Main Packages

```text
app/src/main/java/com/tony/gardenflow/
├── data/
│   ├── local/          Room database, DAOs, entities, converters
│   ├── remote/         DeepSeek, weather, plant research services
│   └── repository/     Repository implementation and mappers
├── domain/
│   ├── engine/         ReminderEngine and GrowthStageCalculator
│   ├── model/          Plant, task, settings, weather models
│   └── repository/     Repository interface
├── location/           Phone location provider
├── notification/       Android notification manager
├── ocr/                ML Kit OCR service
├── security/           Integrity and anti-tamper client framework
├── ui/
│   ├── addplant/       Add plant questionnaire flow
│   ├── components/     Cards, icons, responsive metrics
│   ├── home/           Garden home screen
│   ├── navigation/     App navigation and bottom bar
│   ├── plants/         Plant detail screen
│   ├── settings/       Settings screen
│   └── theme/          Colors, typography, theme
└── worker/             Daily, weather, and snooze workers
```

## Data Model Summary

Core local entities:

- `PlantEntity`: plant identity, care rules, raw packet text, image URI, confirmed stage
- `GrowthStageEntity`: timeline stages per plant
- `GardenTaskEntity`: generated due tasks
- `CareHistoryEntity`: watering, fertilising, skipped or completed actions
- `AppSettingsEntity`: garden location, reminder check time, notification toggle

Important domain fields:

- `Plant.sowingDate`: optional; may be blank for mature nursery plants
- `Plant.confirmedStageKey`: user-confirmed stage overrides seed-date estimation
- `Plant.wateringIntervalDays`: base watering cycle
- `Plant.wateringAmountMm`: recommended watering depth
- `Plant.rainSkipThresholdMm`: rain amount that suppresses watering tasks
- `Plant.fertilisingIntervalDays`: fertilising cycle
- `CareHistory`: drives next due date calculations

## Task Logic

`ReminderEngine` generates tasks from:

- Plants
- Care history
- Weather snapshot
- Current time

Watering task logic:

```text
watering due
AND recent rain below threshold
AND upcoming rain below threshold
→ show watering task
```

High temperature:

```text
today max temperature >= 30°C
→ watering interval is shortened by 1 day
```

Fertilising:

```text
fertilising due
→ show fertilising task
```

Weather failure:

```text
weather unavailable
→ continue using care cycle only
```

Notifications:

- The app checks at the configured task reminder time.
- It only notifies when there are due tasks.
- No tasks means no notification.

## AI Care Plan Flow

The DeepSeek-compatible service returns structured JSON:

- Plant name
- Variety
- Plant icon key
- Watering interval
- Recommended watering amount in mm
- Fertilising interval
- Fertilising advice
- Rain skip threshold
- Preferred temperature range
- Germination and harvest ranges
- Growth stages
- Care notes
- Source summary

The app tells AI whether the user is adding:

- Seed packet / seed
- Seedling
- Young nursery plant
- Established plant
- Mature plant
- Not sure

This matters because Australian garden centres often sell established plants, tube stock, seedlings, bare-root plants, and mature potted plants, not only seeds.

## Navigation

Bottom navigation:

```text
Garden
Settings
```

Secondary screens:

- Add Plant
- Plant Detail

The bottom bar is shown only on primary screens. Add and detail screens use a back arrow.

## UI Design Principles

Current style:

- Calm cream background
- Green primary color
- Rounded cards
- Large typography
- Minimal line icons
- Bottom navigation
- Bilingual labels
- Responsive spacing for smaller phones

Design goals for next upgrade:

- Keep daily actions fast.
- Make plant cards easier to scan.
- Reduce excessive card height where possible.
- Avoid decorative clutter.
- Preserve large touch targets.
- Improve hierarchy between weather, plant status, and actions.
- Keep Chinese and English text lengths in mind.
- Avoid UI overlap on small Samsung screens.

## Icon System

Current icons live in:

```text
app/src/main/res/drawable/
```

Examples:

- `gf_icon_water.png`
- `gf_icon_fertilise.png`
- `gf_icon_lemon.png`
- `gf_icon_potato.png`
- `gf_icon_tomato.png`
- `gf_icon_sunny.png`
- `gf_icon_rainy.png`
- `gf_icon_garden_location.png`

The mapping is centralized in:

```text
app/src/main/java/com/tony/gardenflow/ui/components/GardenIcon.kt
```

Design notes:

- Plant cards should show a plant-specific icon whenever possible.
- Generic plant icons should only be used as fallback.
- Water and fertilise buttons should always show recognizable icons.
- Weather icon should be visible near the home header.
- Designers can replace PNG assets while keeping resource names stable.

## Screen UI Inventory

### 1. Garden Screen

File:

```text
ui/home/HomeScreen.kt
```

Purpose:

The main screen. It combines weather, due task signals, plant list, and quick care actions.

Visible structure:

```text
GardenFlow
Greeting
Hero title
Weather icon

Weather card
Your Plants
Plant cards
Floating add button
Bottom nav: Garden / Settings
```

Header content:

- App name: `GardenFlow`
- Greeting:
  - Good morning / Good afternoon / Good evening / Good night
  - Chinese equivalents on Chinese devices
- Hero title:
  - If watering task exists: `Water {plant name}`
  - Otherwise: current date
- Weather icon:
  - Sunny / partly cloudy / cloudy / rainy / night

Weather card content:

- Location name
- Weather summary
- Current temperature

Examples:

```text
Box Hill, Victoria, Australia
No significant rain expected
11°C
```

Plant card content:

```text
Plant icon
Plant name
Variety
Current stage - Day number
Water in X days / Water today / Overdue by X days

[Water] [Fertilise]
```

Quick actions:

- Water:
  - Records watering in care history
  - Completes open watering task for that plant
- Fertilise:
  - Records fertilising in care history
  - Completes open fertilising task for that plant

Empty states:

- If no plants: currently the list is simply empty.
- Design opportunity: add a friendly empty garden state with one primary add action.

Design upgrade opportunities:

- Make the plant card hierarchy more compact.
- Consider showing due plants first.
- Consider badges for `Due today`, `Rain skipped`, and `Overdue`.
- Consider a clearer “next action” line instead of repeating schedule data.
- Keep action buttons reachable with thumb on Samsung phones.

### 2. Add Plant Screen

File:

```text
ui/addplant/AddPlantScreen.kt
```

Purpose:

Adds a plant through a step-by-step questionnaire. This flow exists because plants may be seeds, seedlings, young nursery plants, established plants, or mature plants.

Top structure:

```text
Back arrow
GardenFlow
Add a plant
Step indicator
Current step content
```

Step 1: Identity

Content:

```text
Scan seed packet
Take a photo or choose an image. AI will build the care plan.

Enter plant name
Type the plant and let AI create a care plan.

Plant name input
Continue
```

Actions:

- Scan seed packet opens image picker.
- Enter plant name reveals manual text input.

Step 2: OCR Review

Only appears when image text is available.

Content:

```text
Review packet text
Check the text before AI creates the plan.

Large editable packet text field
Choose another image
Back
Continue
```

States:

- OCR loading
- OCR failed
- OCR text empty
- Editable text ready

Step 3: Plant State Question

Content:

```text
What are you adding?
This keeps mature nursery plants from being tracked like fresh seeds.

Seed packet / seed
Seedling
Young nursery plant
Established plant
Mature plant
Not sure

Back
Continue
```

Purpose:

The selected state is sent to AI and saved locally. Mature or established plants should not be forced into seed-based day tracking.

Design upgrade opportunities:

- Use illustrated selectable cards instead of chips.
- Explain common Australian nursery formats:
  - Tube stock
  - Seedling punnet
  - Bare root
  - Mature potted plant
- Make “Not sure” reassuring rather than a failure path.

Step 4: Dates

Content:

```text
Plant dates

When was it planted?
YYYY-MM-DD field
Calendar icon
Today / Yesterday / 3 days ago / Not planted yet

Last watered
YYYY-MM-DD field
Calendar icon
Today / Yesterday / 3 days ago / Not recorded

Back
Create care plan
```

Special state:

If user selected established or mature plant:

```text
For established plants, leave planted date blank if you do not know it.
GardenFlow will use your selected current stage instead.
```

AI Loading State:

```text
GardenFlow
AI is building your care plan
Finding care fields
Reading packet information
Understanding the plant
Creating watering schedule
Building growth timeline
This may take a few seconds.
```

Care Plan Review:

```text
Review care plan
Plant name
Variety

Plant information
Care source
Watering
Fertilising
Preferred temperature
Estimated harvest

Add to my garden
Regenerate
```

Design upgrade opportunities:

- Add progress indicator that feels like a guided onboarding flow.
- Make AI result cards editable in a future version.
- Display confidence or “based on your selected plant state”.
- Avoid making the screen too long after AI result appears.

### 3. Plant Detail Screen

File:

```text
ui/plants/PlantDetailScreen.kt
```

Purpose:

Shows the complete care profile and history for one plant.

Top structure:

```text
Back arrow
GardenFlow
More menu
```

Content:

```text
Plant name
Variety

Stage hero card
Growth timeline
Care schedule
Care History
Delete plant
```

Stage hero card:

```text
Plant icon
Current stage label
Day X / Not planted yet
```

Important logic:

- If user confirmed a stage during add flow, that stage is shown.
- If no confirmed stage exists, stage is estimated from sowing date.
- Mature plants may not show seed-based day tracking.

Growth timeline:

```text
Stage marker
Stage label
Est. day start-end
Current stage marker
```

Care schedule:

```text
Water every X days
Recommended water: X mm per watering
Fertilise every X days
Fertilising advice
Skip watering after X mm rain
Temperature: X to Y°C
```

Care history:

```text
WATER - timestamp
FERTILISE - timestamp
```

Empty history:

```text
No care actions yet.
```

Delete:

- Delete button opens confirmation dialog.
- Deletes the plant and generated reminders.

Design upgrade opportunities:

- Make care history human-readable:
  - `Watered yesterday`
  - `Fertilised on 24 Jul`
- Consider tabs or sections if detail page becomes long.
- Improve timeline alignment and current-stage emphasis.
- Add stage calibration UI again in a more polished way.

### 4. Settings Screen

File:

```text
ui/settings/SettingsScreen.kt
```

Purpose:

Controls garden location, task notification time, weather rules, AI config status, and app metadata.

Top structure:

```text
Back arrow
Settings
```

Sections:

```text
Garden
Reminders
Weather
AI
App
```

Garden section:

```text
Garden location
Current location label

City or label
Latitude
Longitude

Use phone location
Save location
```

Behavior:

- User can manually enter city/label and coordinates.
- User can use phone location.
- Location is used for weather-aware watering decisions.

Reminder section:

```text
Task reminder
Only notifies when a plant has a task due.

Reminder check time
HH:mm
Save reminder

Notifications
Enabled / Disabled
Switch
```

Important behavior:

- This is not a daily notification.
- WorkManager checks at the configured time.
- Notification is sent only if generated tasks exist.

Weather section:

```text
High temperature rule
30°C shortens watering interval
```

AI section:

```text
AI detector
AI configured / Not working
```

App section:

```text
Export data
Coming soon

About GardenFlow
Version 1.0.0
```

Design upgrade opportunities:

- Make location setting less technical for non-developers.
- Hide latitude/longitude behind an advanced section.
- Add confirmation snackbar position that is visible above keyboard.
- Make notification behavior clearer: “Only when something is due.”

### 5. Bottom Navigation

File:

```text
ui/navigation/GardenFlowNavHost.kt
```

Current tabs:

```text
Garden
Settings
```

Behavior:

- Appears only on primary screens.
- Hidden on Add Plant and Plant Detail screens.
- Garden tab returns to the home garden screen.
- Settings tab opens settings.

Design upgrade opportunities:

- Consider icons:
  - Garden: plant/checklist hybrid
  - Settings: gear
- Keep labels visible for discoverability.
- Do not add a separate Plants tab unless the home screen no longer includes plant cards.

## Core Components

### GardenCard

File:

```text
ui/components/Common.kt
```

Used throughout the app. Current style:

- Rounded card
- Surface background
- Small elevation
- Internal padding controlled by screen-specific layout

### GardenIconBadge

File:

```text
ui/components/GardenIcon.kt
```

Used for:

- Plant icons
- Setting rows
- Task icons
- Weather icons

### Responsive Metrics

File:

```text
ui/components/Responsive.kt
```

Controls:

- Horizontal screen padding
- Card padding
- List gaps
- Icon size

Purpose:

Reduce layout issues across different Samsung screen widths and font scales.

## Color and Typography

Theme file:

```text
ui/theme/Theme.kt
```

Current palette:

- Garden green primary
- Cream background
- Warm white cards
- Dark green/ink text
- Supports light and dark color schemes

Current typography:

- Large display title for primary screens
- Smaller display sizes than default Material 3 to reduce overflow
- Text uses stable line heights

Design upgrade notes:

- Avoid making every surface the same shade of cream.
- Keep green as the primary action color.
- Purple is currently used on some generated button surfaces; consider replacing or standardizing.
- Preserve contrast for outdoor visibility.

## Bilingual UI

Text helper:

```text
util/GardenText.kt
```

The app currently uses a lightweight language helper:

- English on English devices
- Chinese on Chinese devices

Design implications:

- Chinese labels may be shorter.
- English labels can wrap more easily.
- Buttons should tolerate both languages.
- Avoid fixed-width text containers.

## Weather UX

Weather service:

```text
data/remote/weather/
```

Weather summary currently appears on the Garden screen:

```text
Location
Rain summary
Temperature
Weather icon
```

Weather data affects:

- Watering skip logic
- High-temperature interval adjustment
- Task reasons

Design opportunity:

Show weather as a garden-relevant status, not a generic weather app. Examples:

- `Rain will handle watering`
- `Dry and hot today`
- `Weather unavailable; using schedule`

## Notifications UX

Notification manager:

```text
notification/GardenNotificationManager.kt
```

Channels:

- Garden Tasks
- Weather Updates
- Snoozed Reminders

Current behavior:

- At the reminder check time, WorkManager generates tasks.
- If no tasks exist, no notification is sent.
- If tasks exist, notification summarizes due garden tasks.

Design opportunity:

Create notification text hierarchy:

```text
GardenFlow
2 plants need attention
Lemon - Water
Potato - Fertilise
```

## Security and Google Play Readiness

GardenFlow includes a release hardening layer:

- Network Security Config blocks cleartext HTTP.
- Release builds enable R8 code shrinking and resource shrinking.
- Backup is disabled.
- App integrity service checks:
  - debug state
  - installer source
  - release signing SHA-256
  - common root indicators
  - Play Integrity token availability

Configuration:

```properties
RELEASE_CERT_SHA256=
ENABLE_PLAY_INTEGRITY=false
```

Important:

Client-side anti-piracy is not absolute. Strong Play Integrity decisions should be verified by a backend.

## Configuration

Copy:

```bash
cp local.properties.example local.properties
```

Fill in:

```properties
DEEPSEEK_API_URL=
DEEPSEEK_API_KEY=
DEEPSEEK_MODEL=
RELEASE_CERT_SHA256=
ENABLE_PLAY_INTEGRITY=false
```

Do not commit `local.properties`.

## Build Locally

```bash
chmod +x gradlew
./gradlew clean
./gradlew test
./gradlew assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK:

```bash
./gradlew assembleRelease
```

```text
app/build/outputs/apk/release/app-release.apk
```

## Release Signing

Copy:

```bash
cp keystore.properties.example keystore.properties
```

Fill in:

```properties
storeFile=
storePassword=
keyAlias=
keyPassword=
```

Build:

```bash
./gradlew assembleRelease
```

Keep the keystore backed up. Android app updates require the same package name and signing key.

## Install APK on Samsung Phone

1. Download APK.
2. Open Samsung My Files.
3. Tap the APK.
4. Allow unknown app installation for the current source.
5. Install.

For Google Play release, prefer Play Console internal testing instead of manual sideload.

## GitHub Actions

Workflow:

```text
.github/workflows/android-build.yml
```

It should:

- Check out source
- Set up JDK
- Run tests
- Build APK
- Upload APK artifact

## Designer Handoff Checklist

Before redesigning, review:

- Garden screen plant card hierarchy
- Add Plant questionnaire flow
- Mature plant vs seed UX
- Weather card hierarchy
- Bottom navigation labels and icons
- Settings simplification
- Bilingual text behavior
- Small Samsung screen behavior
- Notification mental model
- Icon asset consistency

Primary redesign targets:

1. Make the Garden screen feel like a living task dashboard.
2. Make Add Plant feel like a friendly guided questionnaire.
3. Make plant cards compact but information-rich.
4. Make weather feel garden-specific.
5. Make settings non-technical for normal users.

## Known Limitations

- The image flow uses Android image selection instead of a full custom CameraX camera screen.
- AI care plans are editable only indirectly; a full editor is not yet implemented.
- Export Data and Clear All Data are placeholders.
- Weather results are not yet cached in a dedicated weather table.
- Play Integrity backend verification is not implemented yet.
