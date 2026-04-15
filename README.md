# StatsApp

Android app for volleyball stat collection.

## Current Implementation

The first screen is implemented and supports quick tap logging for:

- Serve quality: `0` to `4`
- Serve receive quality: `0` to `3`
- Attack outcome: `Kill`, `Attempt`, `Error`
- Set outcome: `Assist`, `Attempt`, `Error`

Every tap creates a timestamped event in a running session list. You can clear the list with **Clear Session**.

## Open In Android Studio

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or Android device.
