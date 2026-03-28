<!-- Light mode logo -->
<img src="https://github.com/user-attachments/assets/e4bc23c1-d7d2-4929-81a3-e7e6e7eae0b0#gh-light-mode-only" width="400">

<!-- Dark mode logo -->
<img src="https://github.com/user-attachments/assets/9424ebe9-8910-4503-a390-90a8ac9af6c6#gh-dark-mode-only" width="400">

Unoffcial Fortnite companion app built with Jetpack Compose and Material Design 3

[![Latest release](https://img.shields.io/github/v/release/Djihads80/ShopNite?label=Release&logo=github)](https://github.com/Djihads80/ShopNite/releases/latest)
[![GitHub License](https://img.shields.io/github/license/Djihads80/ShopNite?logo=gnu)](/LICENSE)
[![AI Slop Inside](https://sladge.net/badge.svg)](https://sladge.net)

# Features


* Wishlisht system with notifications for returning items in the shop
* Cosmetic browser for all cosmetics, new cosmetics and wishlists
* Battle Royale Summary
* Item Shop browser
* Battle Royale News

# Installation
Go to the releases page [or here](https://github.com/Djihads80/ShopNite/releases/latest) and download an install the APK like you normally would

After it installs, open the app and accept notification permissions, then you can go to Settings and add your username for BR Summary if you want to use it

# Build Instructions

### Requirements

* Java JDK 17 (recommended)
* Android Studio (latest)
* Android SDK installed

### Clone the repository

```bash
git clone https://github.com/Djihads80/ShopNite.git
cd ShopNite
```

### Firebase push setup

Firebase Cloud Messaging is optional, but the app now expects a local `app/google-services.json` file if you want the Firebase-enabled build.

1. Create your own Firebase project and add an Android app with package name `com.djihad.shopnite`
2. Download `google-services.json`
3. Place it at `app/google-services.json`


### Fortnite-API key setup

Get a key from https://dash.fortnite-api.com/account by logging in with your Discord account and following the instructions

The easiest local setup is to add your key to `local.properties`:

```properties
sdk.dir=/path/to/your/android/sdk
fortniteApiKey=your-fortnite-api-key
```

You can also provide it through an environment variable when building:

```bash
FORTNITE_API_KEY=your-fortnite-api-key ./gradlew assembleDebug
```

### Build using Gradle (CLI)

#### Debug APK

```bash
./gradlew assembleDebug
```

#### Release APK

```bash
./gradlew assembleRelease
```

The generated APK will be located in:

```
app/build/outputs/apk/
```


### Run in Android Studio

1. Open the project in Android Studio
2. Let Gradle sync
3. Click **Run ▶️**
