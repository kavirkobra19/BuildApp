# Kavir Downloader

Native Android downloader built with Java and Gradle.

## Version 1.1.0
- Uses the Android yt-dlp engine (youtubedl-android 0.18.1) for supported media-page extraction and downloads.
- Bundles FFmpeg for merging compatible audio/video streams.
- Attempts to update the yt-dlp engine to the NIGHTLY channel at startup; the bundled engine remains available if the update cannot be reached.
- Shows download progress in the app.
- Keeps Android DownloadManager for direct HTTP/HTTPS file URLs.
- Saves yt-dlp output under `Downloads/KavirDownloader`.
- Retains the dark purple interface and local VLESS/VMess/Trojan profile field.

## Supported media and limitations
yt-dlp supports many sites, but extraction depends on the current site implementation, public availability, account/session requirements, and the installed extractor version. Not every link or format is guaranteed to work. Only download content you own or are authorized to save.

Spotify subscription/offline tracks are DRM-protected and are not exported as MP3 by this app. The proxy profile field currently stores text only; it does not run Xray/V2Ray or route app traffic. The GitHub YT-Kavir workflow uses a separate GitHub-hosted Ubuntu runner and a WARP SOCKS5 container; that server-side network setup is not embedded in this Android build.

## Build
GitHub Actions builds a debug APK on every push to `main` and on manual dispatch. Download the `KavirDownloader-debug` artifact from the workflow run, or install the APK attached to the generated Release.

- Application ID: `com.kavir.downloader`
- Minimum Android: 6.0 (API 23)
- Compile/target SDK: 35
- Java: 17

## Third-party licensing
The youtubedl-android dependency is GPL-3.0. Review and comply with its license obligations when distributing modified binaries/source.
