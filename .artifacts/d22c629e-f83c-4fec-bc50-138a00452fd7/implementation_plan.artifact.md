# Fix Firebase Dependency Resolution Issue

The project is failing to sync because `com.google.firebase:firebase-auth-ktx` cannot be resolved. This is likely because Firebase has deprecated and stopped publishing separate `-ktx` artifacts in favor of including Kotlin extensions directly in the main artifacts (starting from BOM 32.0.0).

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///E:/LifeTrackerTimeline/gradle/libs.versions.toml)
- Update Firebase library definitions to use the main artifacts instead of the `-ktx` variants.
- Changed libraries:
    - `firebase-auth`
    - `firebase-firestore`
    - `firebase-analytics`
    - `firebase-crashlytics`

## Verification Plan

### Automated Tests
- Run Gradle sync to verify that all dependencies are resolved correctly.
- Run a clean build to ensure no compilation errors occur due to the change (though modern Firebase artifacts include the KTX classes, so it should be seamless).

### Manual Verification
- Deploy the app to a device/emulator to ensure Firebase features (Auth, Firestore, etc.) still work as expected.
