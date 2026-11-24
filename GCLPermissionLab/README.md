# GCL Permission Lab

A demonstration app for user-controlled camera, microphone, and location access.

## Safety and behavior
- Camera preview starts only after the user presses **Start camera** and grants camera permission.
- Recording starts only after the user presses **Start recording** and grants microphone permission.
- Location is requested only after the user presses **Get current location** and grants location permission.
- Recording is saved locally as `gcl-recording.m4a` in the app's private files directory.
- The app has no background capture, network transmission, or playback network access.

## Features
- CameraX live preview with Start/Stop controls.
- MediaRecorder microphone capture with Start/Stop and local playback controls.
- Fused location provider with latitude, longitude, and accuracy.

## Open in Android Studio
1. Extract the ZIP.
2. Open Android Studio → Open → select the `GCLPermissionLab` folder.
3. Allow Gradle sync. Android Studio may download the Android Gradle Plugin and dependencies.
4. Use JDK 17 (Android Studio's bundled JBR is suitable).
5. Build → Build Bundle(s) / APK(s) → Build APK(s).

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Build an APK with GitHub Actions
The `Build Android Debug APK` workflow (`.github/workflows/build-apk.yml`) builds the debug APK on pushes, pull requests, or when started manually from the repository's Actions tab. When adding this folder to GitHub, preserve the outer folder structure so `.github/` and `GCLPermissionLab/` are both at the repository root. The workflow uses JDK 17, Gradle 8.9, and Android SDK 35. The Android Gradle Plugin signs the debug APK with its standard debug key; no signing secrets or setup are required for installing this build on a device. The workflow verifies the package ID, APK ZIP integrity, and Android APK signature before uploading.

After a successful run, open the run in GitHub → **Artifacts** → download `GCL-Permission-Lab-debug`. The downloaded archive contains `app-debug.apk`. Debug builds are for testing and are not suitable for publishing to the Play Store.

## Install over wireless ADB
First ensure `adb devices` lists your own phone with status `device` (not `offline`).

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If running the command from Windows CMD, use the full Windows path to the APK.

## Package ID and installation
The app keeps package ID `com.gcl.permissionlab`.

If upgrading from an APK signed with a different certificate, Android may require uninstalling the old app first. Samsung may also require allowing the app used to open the APK (for example, My Files or Chrome) to **Install unknown apps** in Settings → Security and privacy → More security settings → Install unknown apps. The exact menu labels vary by phone and Android version.

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
