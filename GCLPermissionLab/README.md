# GCL Permission Lab

A small Android training app for demonstrating Android runtime permission prompts.

## Safety and behavior
- Requests camera, microphone, and foreground location permission only after the user taps the matching button.
- Displays whether Android reports each permission as granted.
- Does **not** open the camera, record audio, retrieve coordinates, run in the background, or transmit data.
- Uses no internet permission.

## Open in Android Studio
1. Extract the ZIP.
2. Open Android Studio → Open → select the `GCLPermissionLab` folder.
3. Allow Gradle sync. Android Studio may download the Android Gradle Plugin and dependencies.
4. Use JDK 17 (Android Studio's bundled JBR is suitable).
5. Build → Build Bundle(s) / APK(s) → Build APK(s).

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Build an APK with GitHub Actions
The `Build Android Debug APK` workflow (`.github/workflows/build-apk.yml`) builds the debug APK on pushes, pull requests, or when started manually from the repository's Actions tab. When adding this folder to GitHub, preserve the outer folder structure so `.github/` and `GCLPermissionLab/` are both at the repository root. The workflow uses JDK 17, Gradle 8.9, and Android SDK 35. The Android Gradle Plugin signs the debug APK with its standard debug key; no signing secrets or setup are required for installing this build on a device.

After a successful run, open the run in GitHub → **Artifacts** → download `GCL-Permission-Lab-debug`. The downloaded archive contains `app-debug.apk`. Debug builds are for testing and are not suitable for publishing to the Play Store.

## Install over wireless ADB
First ensure `adb devices` lists your own phone with status `device` (not `offline`).

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If running the command from Windows CMD, use the full Windows path to the APK.

## Package ID
`com.gcl.permissionlab`

## Inspect declared permissions
```bash
adb shell dumpsys package com.gcl.permissionlab
```

To reset permissions for another recording take:
```bash
adb shell pm revoke com.gcl.permissionlab android.permission.CAMERA
adb shell pm revoke com.gcl.permissionlab android.permission.RECORD_AUDIO
adb shell pm revoke com.gcl.permissionlab android.permission.ACCESS_COARSE_LOCATION
adb shell pm revoke com.gcl.permissionlab android.permission.ACCESS_FINE_LOCATION
```
