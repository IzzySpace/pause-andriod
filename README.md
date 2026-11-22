# Pause for Android

Pause helps you set up an adult-site filter and gives you a one-minute reset when an urge hits. Optional check-ins record a trigger and a response. Pause has no account, analytics, or browsing history. It keeps up to 30 check-ins in private app storage and lets you clear them.

## Run the app

Use Android Studio with a JDK 17 or newer and Android SDK Platform 35. Open this directory, sync Gradle, choose an Android 9 (API 28) or newer device, and run the `app` configuration.

To build a debug APK from the terminal, run `./gradlew :app:assembleDebug` on macOS or Linux, or `gradlew.bat :app:assembleDebug` on Windows. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Check the filter

1. Open **Set up or test blocking** and copy the Cloudflare for Families hostname.
2. Open Android Settings, search for **Private DNS**, choose the provider hostname option, paste the copied hostname, and save.
3. Check that an ordinary website loads. Then use **Open safe test page** and confirm that the Cloudflare test domain is blocked. Repeat on Wi-Fi and mobile data. A browser configured to use a different secure DNS provider may behave differently.

Android applies the hostname setting after you save it. You can restore your previous Private DNS setting in Android Settings. The filter blocks domains and cannot distinguish individual posts inside social apps.

## Install Pause Preview

The debug APK installs as Pause Preview alongside the original Pause app. Both use the phone’s system-wide Private DNS setting. Each app keeps its own check-ins.
