# Kavir Downloader

Android downloader starter project built with Java and Gradle.

## Version 1.0.1
- Validates HTTP/HTTPS URLs and gives clearer feedback for platform webpage links.
- Downloads direct media/file URLs through Android DownloadManager.
- Adds a separate **Save to Gallery** action. Images/GIFs are placed under Pictures/KavirDownloader and videos under Movies/KavirDownloader; other files go to Downloads/KavirDownloader.
- Keeps the dark purple mobile interface and local VLESS/VMess/Trojan profile text setting.

## Important limitations
A YouTube, Instagram, TikTok, Spotify or Pinterest page URL is not itself a direct media-file URL. This version does not contain platform extractors. Paste an authorized direct media URL where available. It does not download DRM-protected Spotify audio or bypass platform access controls.

The proxy profile field stores text locally only. It does not run Xray/V2Ray or route app traffic. A later implementation needs an Android-compatible Xray core, lifecycle management and a per-app proxy networking design. This is not a device-wide VPN.

## Build
GitHub Actions builds a debug APK on every push to `main` and on manual dispatch. Download the `KavirDownloader-debug` artifact from the workflow run, or install the APK attached to the generated Release.

- Application ID: `com.kavir.downloader`
- Minimum Android: 6.0 (API 23)
- Compile/target SDK: 35
- Java: 17
