package com.kavir.downloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.webkit.URLUtil;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = 0xFF100D18;
    private static final int PANEL = 0xFF21182D;
    private static final int PURPLE = 0xFF9B5DE5;
    private static final int WHITE = 0xFFF5F0FA;
    private static final int MUTED = 0xFFB8AFC4;
    private EditText urlInput;
    private TextView status;
    private android.content.SharedPreferences prefs;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("kavir_downloader", MODE_PRIVATE);
        buildUi();
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
        brand.setTextColor(PURPLE);
        brand.setTextSize(13);
        brand.setLetterSpacing(0.12f);
        root.addView(brand);

        TextView title = new TextView(this);
        title.setText("Download anything\nyou can access.");
        title.setTextColor(WHITE);
        title.setTextSize(29);
        title.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        title.setPadding(0, dp(18), 0, dp(8));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Paste a direct media or file URL. Downloads are handled by Android.");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);
        root.addView(subtitle);

        TextView inputLabel = label("SOURCE URL");
        inputLabel.setPadding(0, dp(28), 0, dp(9));
        root.addView(inputLabel);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextColor(WHITE);
        urlInput.setHintTextColor(0xFF8E849B);
        urlInput.setHint("https://example.com/file.mp4");
        urlInput.setTextSize(14);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setPadding(dp(14), dp(13), dp(14), dp(13));
        urlInput.setBackgroundColor(PANEL);
        root.addView(urlInput, new LinearLayout.LayoutParams(-1, dp(54)));

        Button check = makeButton("CHECK LINK", false);
        check.setOnClickListener(v -> checkLink());
        addGap(root, 12);
        root.addView(check, fullWidth());

        Button download = makeButton("DOWNLOAD  ↓", true);
        download.setOnClickListener(v -> startDownload());
        addGap(root, 10);
        root.addView(download, fullWidth());

        status = new TextView(this);
        status.setText("READY · Waiting for a link");
        status.setTextColor(MUTED);
        status.setTextSize(13);
        status.setPadding(dp(14), dp(16), dp(14), dp(16));
        status.setBackgroundColor(PANEL);
        addGap(root, 18);
        root.addView(status, fullWidth());

        TextView note = new TextView(this);
        note.setText("SUPPORTED INPUT\nDirect HTTP / HTTPS file URLs\n\nPLATFORM LINKS\nYouTube · Instagram · TikTok · Spotify · Pinterest\nThese require a supported, authorized direct media URL. Platform extraction is not included in this build.");
        note.setTextColor(MUTED);
        note.setTextSize(12);
        note.setLineSpacing(dp(4), 1f);
        note.setPadding(dp(14), dp(16), dp(14), dp(16));
        note.setBackgroundColor(PANEL);
        addGap(root, 14);
        root.addView(note, fullWidth());

        Button settings = makeButton("SETTINGS  ⚙", false);
        settings.setOnClickListener(v -> showSettings());
        addGap(root, 20);
        root.addView(settings, fullWidth());

        TextView footer = new TextView(this);
        footer.setText("KAVIR DOWNLOADER  •  v1.0.0");
        footer.setTextColor(0xFF756B80);
        footer.setTextSize(10);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(26), 0, 0);
        root.addView(footer, fullWidth());
        setContentView(scroll);
    }

    private void checkLink() {
        String value = urlInput.getText().toString().trim();
        if (!isHttpUrl(value)) {
            setStatus("INVALID URL · Enter a valid http:// or https:// address", true);
            return;
        }
        String host = Uri.parse(value).getHost();
        String lower = host == null ? "" : host.toLowerCase(Locale.ROOT);
        String type = "DIRECT LINK · Ready to download";
        if (lower.contains("youtube.com") || lower.equals("youtu.be")) type = "YOUTUBE LINK · Platform extraction is not included";
        else if (lower.contains("instagram.com")) type = "INSTAGRAM LINK · Platform extraction is not included";
        else if (lower.contains("tiktok.com")) type = "TIKTOK LINK · Platform extraction is not included";
        else if (lower.contains("spotify.com") || lower.contains("spotify.link")) type = "SPOTIFY LINK · Protected audio cannot be extracted";
        else if (lower.contains("pinterest.")) type = "PINTEREST LINK · Platform extraction is not included";
        setStatus(type, false);
    }

    private void startDownload() {
        String value = urlInput.getText().toString().trim();
        if (!isHttpUrl(value)) {
            setStatus("INVALID URL · Check the address first", true);
            return;
        }
        String host = Uri.parse(value).getHost();
        String lower = host == null ? "" : host.toLowerCase(Locale.ROOT);
        if (lower.contains("youtube.com") || lower.equals("youtu.be") || lower.contains("instagram.com") || lower.contains("tiktok.com") || lower.contains("spotify.com") || lower.contains("spotify.link") || lower.contains("pinterest.")) {
            setStatus("PLATFORM LINK · Paste an authorized direct media URL instead", true);
            return;
        }
        try {
            String fileName = URLUtil.guessFileName(value, null, null);
            if (fileName == null || fileName.trim().isEmpty()) fileName = "download.bin";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(value));
            request.setTitle(fileName);
            request.setDescription("Downloading with Kavir Downloader");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "KavirDownloader/" + fileName);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) throw new IllegalStateException("Download service unavailable");
            manager.enqueue(request);
            setStatus("DOWNLOAD QUEUED · " + fileName, false);
            Toast.makeText(this, "Download added", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            setStatus("DOWNLOAD ERROR · " + e.getMessage(), true);
        }
    }

    private void showSettings() {
        EditText profile = new EditText(this);
        profile.setText(prefs.getString("proxy_profile", ""));
        profile.setHint("vless://, vmess:// or trojan://");
        profile.setTextColor(WHITE);
        profile.setHintTextColor(MUTED);
        profile.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        profile.setMinLines(3);
        profile.setPadding(dp(12), dp(10), dp(12), dp(10));
        new AlertDialog.Builder(this)
                .setTitle("Proxy profile")
                .setMessage("Profile text is stored locally only. This version does not connect a VPN or proxy tunnel.")
                .setView(profile)
                .setNegativeButton("CANCEL", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("proxy_profile").apply())
                .setPositiveButton("SAVE", (d, w) -> {
                    String value = profile.getText().toString().trim();
                    if (value.startsWith("vless://") || value.startsWith("vmess://") || value.startsWith("trojan://")) {
                        prefs.edit().putString("proxy_profile", value).apply();
                        Toast.makeText(this, "Profile saved locally (not connected)", Toast.LENGTH_LONG).show();
                    } else if (!value.isEmpty()) {
                        Toast.makeText(this, "Unsupported profile format", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private boolean isHttpUrl(String value) {
        if (value == null || !(value.startsWith("https://") || value.startsWith("http://"))) return false;
        Uri uri = Uri.parse(value);
        return uri.getHost() != null && uri.getHost().contains(".");
    }

    private void setStatus(String message, boolean error) {
        status.setText(message);
        status.setTextColor(error ? 0xFFFF7777 : 0xFFBDA1FF);
    }

    private TextView label(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(PURPLE);
        view.setTextSize(11);
        view.setLetterSpacing(0.1f);
        return view;
    }

    private Button makeButton(String text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFF7138B8 : PANEL));
        return button;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(-1, dp(52));
    }

    private void addGap(LinearLayout root, int height) {
        View gap = new View(this);
        root.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
