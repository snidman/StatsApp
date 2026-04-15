# StatsApp

Android app for volleyball stat collection.

## Current Implementation

The app now supports full player-based stat tracking with local persistence.

### Player-based Live Entry

- Add players (name + jersey).
- Capture stats for all players on one screen.
- Per-player stat buttons show live counts.

### Tracked Skills

- Serve quality: `0` to `4`
- Serve receive quality: `0` to `3`
- Attack outcome: `KILL`, `ATTEMPT`, `ERROR`
- Set outcome: `ASSIST`, `ATTEMPT`, `ERROR`

### Persistence and Filters

- Uses Room database for local persistence.
- Sessions survive app restarts.
- Match filter and set filter (`All`, `1..5`) are available on screen.
- Create additional matches directly in the app.
- Delete the selected match.
- Delete the selected set's events.
- Delete individual players.

### CSV Export

- Export currently filtered events to CSV using Android's file picker.
- CSV columns: `player_name`, `jersey_number`, `match`, `set`, `skill`, `outcome`, `timestamp`.

### Event Feed

- Recent filtered events are shown in-app with player and timestamp details.

## Open In Android Studio

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or Android device.
