package com.kavir.downloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.ffmpeg.FFmpeg;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = 0xFF100D18, PANEL = 0xFF21182D,
            PURPLE = 0xFF9B5DE5, WHITE = 0xFFF5F0FA, MUTED = 0xFFB8AFC4;
    private EditText urlInput;
    private TextView status;
    private android.content.SharedPreferences prefs;
    private boolean mediaEngineReady = false;
    private volatile boolean downloading = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("kavir_downloader", MODE_PRIVATE);
        buildUi();
        initializeMediaEngine();
    }

    private void initializeMediaEngine() {
        new Thread(() -> {
            try {
                YoutubeDL.getInstance().init(getApplication());
                FFmpeg.getInstance().init(getApplication());
                mediaEngineReady = true;
                runOnUiThread(() -> setStatus("READY · yt-dlp media engine initialized", false));
            } catch (Exception e) {
                mediaEngineReady = false;
                runOnUiThread(() -> setStatus("MEDIA ENGINE ERROR · " + errorText(e), true));
            }
        }).start();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(24));
        root.setBackgroundColor(BG);
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("KAVIR / DOWNLOADER");
        brand.setTextColor(PURPLE); brand.setTextSize(13); brand.setLetterSpacing(.12f);
        root.addView(brand);

        TextView title = new TextView(this);
        title.setText("Download anything\\nyou can access.");
        title.setTextColor(WHITE); title.setTextSize(29);
        title.setTypeface(android.graphics.Typeface.create("sans-serif", 1));
        title.setPadding(0, dp(18), 0, dp(8)); root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Paste a supported media link or a direct file URL.");
        subtitle.setTextColor(MUTED); subtitle.setTextSize(14); root.addView(subtitle);

        TextView inputLabel = label("SOURCE URL");
        inputLabel.setPadding(0, dp(28), 0, dp(9)); root.addView(inputLabel);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true); urlInput.setTextColor(WHITE);
        urlInput.setHintTextColor(0xFF8E849B);
        urlInput.setHint("https://..."); urlInput.setTextSize(14);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setPadding(dp(14), dp(13), dp(14), dp(13));
        urlInput.setBackgroundColor(PANEL);
        root.addView(urlInput, new LinearLayout.LayoutParams(-1, dp(54)));

        Button check = makeButton("CHECK LINK", false);
        check.setOnClickListener(v -> checkLink()); addGap(root, 12); root.addView(check, fullWidth());

        Button download = makeButton("DOWNLOAD  ↓", true);
        download.setOnClickListener(v -> startDownload(false)); addGap(root, 10); root.addView(download, fullWidth());

        Button save = makeButton("SAVE TO GALLERY  ▣", false);
        save.setOnClickListener(v -> startDownload(true)); addGap(root, 8); root.addView(save, fullWidth());

        status = new TextView(this);
        status.setText("STARTING MEDIA ENGINE…"); status.setTextColor(MUTED);
        status.setTextSize(13); status.setPadding(dp(14), dp(16), dp(14), dp(16));
        status.setBackgroundColor(PANEL); addGap(root, 18); root.addView(status, fullWidth());

        TextView note = new TextView(this);
        note.setText("MEDIA ENGINE\\nyt-dlp + FFmpeg\\n\\nSUPPORTED SOURCES\\nYouTube · Instagram · TikTok · Pinterest · Direct media URLs\\n\\nSpotify DRM-protected tracks cannot be exported. Only download content you own or have permission to save.");
        note.setTextColor(MUTED); note.setTextSize(12); note.setLineSpacing(dp(4), 1f);
        note.setPadding(dp(14), dp(16), dp(14), dp(16)); note.setBackgroundColor(PANEL);
        addGap(root, 14); root.addView(note, fullWidth());

        Button settings = makeButton("SETTINGS  ⚙", false);
        settings.setOnClickListener(v -> showSettings()); addGap(root, 20); root.addView(settings, fullWidth());

        TextView footer = new TextView(this);
        footer.setText("KAVIR DOWNLOADER  •  v1.1.2");
        footer.setTextColor(0xFF756B80); footer.setTextSize(10);
        footer.setGravity(Gravity.CENTER); footer.setPadding(0, dp(26), 0, 0);
        root.addView(footer, fullWidth());
        setContentView(scroll);
    }

    private void checkLink() {
        String value = urlInput.getText().toString().trim();
        if (!isHttpUrl(value)) { setStatus("INVALID URL · Enter a valid http:// or https:// address", true); return; }
        String host = Uri.parse(value).getHost().toLowerCase(Locale.ROOT);
        String platform = platformName(host);
        if ("SPOTIFY".equals(platform)) {
            setStatus("SPOTIFY · Protected music cannot be downloaded as audio. Open the track in Spotify.", true);
        } else if (platform != null) {
            setStatus(platform + " LINK · yt-dlp will try to extract available media formats.", false);
        } else {
            setStatus("DIRECT LINK · Ready to download if the server allows file access.", false);
        }
    }

    private void startDownload(boolean gallery) {
        String value = urlInput.getText().toString().trim();
        if (!isHttpUrl(value)) { setStatus("INVALID URL · Check the address first", true); return; }
        if (downloading) { setStatus("DOWNLOAD IN PROGRESS · Please wait", true); return; }
        String host = Uri.parse(value).getHost().toLowerCase(Locale.ROOT);
        String platform = platformName(host);
        if ("SPOTIFY".equals(platform)) {
            setStatus("SPOTIFY · DRM-protected tracks are not exportable. Use Spotify's official offline feature.", true);
            return;
        }
        if (platform != null) {
            if (!mediaEngineReady) { setStatus("MEDIA ENGINE NOT READY · Wait for initialization or reopen the app", true); return; }
            downloadWithYtDlp(value, gallery);
        } else {
            downloadDirect(value, gallery);
        }
    }

    private void downloadWithYtDlp(String url, boolean gallery) {
        downloading = true;
        setStatus("ANALYZING · yt-dlp is checking available formats…", false);
        new Thread(() -> {
            try {
                File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "KavirDownloader");
                if (!outputDir.exists() && !outputDir.mkdirs()) {
                    throw new IllegalStateException("Cannot create Downloads/KavirDownloader");
                }

                YoutubeDLRequest request = new YoutubeDLRequest(url);
                request.addOption("-f", "best[ext=mp4]/best");
                request.addOption("--merge-output-format", "mp4");
                request.addOption("--no-playlist");
                request.addOption("--newline");
                request.addOption("--no-mtime");
                request.addOption("--retries", "5");
                request.addOption("--fragment-retries", "5");
                request.addOption("-o", new File(outputDir, "%(title).150B.%(ext)s").getAbsolutePath());

                YoutubeDL.getInstance().execute(request, null, (progress, etaInSeconds, output) -> {
                    runOnUiThread(() -> setStatus(
                            "DOWNLOADING · " + Math.round(progress) + "% · ETA " + etaInSeconds + "s", false));
                    return kotlin.Unit.INSTANCE;
                });

                runOnUiThread(() -> {
                    downloading = false;
                    setStatus("DOWNLOAD COMPLETE · Check Downloads/KavirDownloader", false);
                    Toast.makeText(this, "Download complete", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    downloading = false;
                    setStatus("YT-DLP ERROR · " + errorText(e), true);
                });
            }
        }).start();
    }

    private void downloadDirect(String value, boolean gallery) {
        try {
            String fileName = URLUtil.guessFileName(value, null, null);
            if (fileName == null || fileName.trim().isEmpty()) fileName = "download.bin";
            String lower = fileName.toLowerCase(Locale.ROOT);
            boolean image = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif");
            boolean video = lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                    lower.endsWith(".webm") || lower.endsWith(".3gp") || lower.endsWith(".mov");
            String folder = gallery ? (image ? Environment.DIRECTORY_PICTURES :
                    (video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_DOWNLOADS)) :
                    Environment.DIRECTORY_DOWNLOADS;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(value));
            request.setTitle(fileName);
            request.setDescription(gallery ? "Saving with Kavir Downloader" : "Downloading with Kavir Downloader");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(folder, "KavirDownloader/" + fileName);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) throw new IllegalStateException("Download service unavailable");
            manager.enqueue(request);
            setStatus((gallery ? "SAVING · " : "DOWNLOAD QUEUED · ") + fileName, false);
        } catch (Exception e) {
            setStatus("DOWNLOAD ERROR · " + errorText(e), true);
        }
    }

    private String platformName(String host) {
        if (host.equals("youtu.be") || host.endsWith("youtube.com") || host.endsWith("youtube-nocookie.com")) return "YOUTUBE";
        if (host.endsWith("instagram.com")) return "INSTAGRAM";
        if (host.endsWith("tiktok.com")) return "TIKTOK";
        if (host.endsWith("spotify.com") || host.equals("spotify.link")) return "SPOTIFY";
        if (host.contains("pinterest.")) return "PINTEREST";
        return null;
    }

    private void showSettings() {
        EditText profile = new EditText(this);
        profile.setText(prefs.getString("proxy_profile", ""));
        profile.setHint("vless://, vmess:// or trojan://");
        profile.setTextColor(WHITE); profile.setHintTextColor(MUTED);
        profile.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        profile.setMinLines(3); profile.setPadding(dp(12), dp(10), dp(12), dp(10));
        new AlertDialog.Builder(this).setTitle("Proxy profile")
                .setMessage("Proxy profile storage only. Xray/V2Ray connection is not enabled in this build.")
                .setView(profile).setNegativeButton("CANCEL", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("proxy_profile").apply())
                .setPositiveButton("SAVE", (d, w) -> {
                    String value = profile.getText().toString().trim();
                    if (value.isEmpty()) { prefs.edit().remove("proxy_profile").apply(); return; }
                    if (value.startsWith("vless://") || value.startsWith("vmess://") || value.startsWith("trojan://")) {
                        prefs.edit().putString("proxy_profile", value).apply();
                        Toast.makeText(this, "Profile saved locally (not connected)", Toast.LENGTH_LONG).show();
                    } else Toast.makeText(this, "Use a VLESS, VMess or Trojan link", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private boolean isHttpUrl(String value) {
        if (value == null || !(value.startsWith("https://") || value.startsWith("http://"))) return false;
        Uri uri = Uri.parse(value);
        return uri.getHost() != null && uri.getHost().contains(".");
    }
    private String errorText(Exception e) {
        String message = e.getMessage();
        return message == null || message.trim().isEmpty() ? e.getClass().getSimpleName() : message;
    }
    private void setStatus(String message, boolean error) {
        if (status != null) {
            status.setText(message);
            status.setTextColor(error ? 0xFFFF7777 : 0xFFBDA1FF);
        }
    }
    private TextView label(String value) {
        TextView view = new TextView(this); view.setText(value); view.setTextColor(PURPLE);
        view.setTextSize(11); view.setLetterSpacing(.1f); return view;
    }
    private Button makeButton(String text, boolean primary) {
        Button button = new Button(this); button.setText(text); button.setAllCaps(false);
        button.setTextSize(13); button.setTextColor(WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFF7138B8 : PANEL));
        return button;
    }
    private LinearLayout.LayoutParams fullWidth() { return new LinearLayout.LayoutParams(-1, dp(52)); }
    private void addGap(LinearLayout root, int height) {
        View gap = new View(this); root.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
