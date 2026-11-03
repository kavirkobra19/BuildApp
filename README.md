# Kavir Downloader

Android downloader starter project built with Java and Gradle.

## Included in this build
- Direct HTTP/HTTPS downloads through Android DownloadManager.
- Dark purple interface with URL validation and link-type hints.
- Downloads are saved under `Downloads/KavirDownloader`.
- A settings dialog can store a VLESS/VMess/Trojan profile string locally.

## Important limitations
This build does **not** include platform extractors for YouTube, Instagram, TikTok, Spotify or Pinterest. It does not download protected Spotify audio, bypass DRM, or evade platform access controls. For those sites, the app only identifies the platform and explains that an authorized direct media URL is required.

The proxy screen currently stores profile text only. It does **not** start a VPN tunnel or route app traffic; a real Xray/V2Ray core and Android `VpnService` integration are still required.

## Build
GitHub Actions builds a debug APK on every push to `main` and on manual dispatch. Download `KavirDownloader-debug` from the workflow run's Artifacts section.

- Application ID: `com.kavir.downloader`
- Minimum Android: 6.0 (API 23)
- Compile/target SDK: 35
- Java: 17
