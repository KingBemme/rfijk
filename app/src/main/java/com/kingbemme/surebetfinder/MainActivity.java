package com.kingbemme.surebetfinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private final ExecutorService network = Executors.newFixedThreadPool(6);

    private static final String UI_GZIP_BASE64 = "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...";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.addJavascriptInterface(new NativeBridge(), "Native");
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL("https://app.local/", inflateUi(), "text/html", "UTF-8", null);
    }

    private String inflateUi() {
        try {
            byte[] compressed = Base64.decode(UI_GZIP_BASE64, Base64.DEFAULT);
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = gzip.read(buf)) != -1) out.write(buf, 0, n);
                return out.toString(StandardCharsets.UTF_8.name());
            }
        } catch (Exception e) {
            return "<html><body style='background:#07110e;color:white;font-family:sans-serif;padding:24px'><h2>SureBet Live</h2><p>UI konnte nicht geladen werden.</p></body></html>";
        }
    }

    private void sendResponse(String requestId, int status, String body, String remaining, String used, String last) {
        final String js = "window.__nativeResponse(" + JSONObject.quote(requestId) + "," + status + "," + JSONObject.quote(body == null ? "" : body) + "," + JSONObject.quote(remaining == null ? "" : remaining) + "," + JSONObject.quote(used == null ? "" : used) + "," + JSONObject.quote(last == null ? "" : last) + ");";
        runOnUiThread(() -> webView.evaluateJavascript(js, null));
    }

    public class NativeBridge {
        @JavascriptInterface
        public void get(String url, String requestId) {
            if (url == null || requestId == null || !url.startsWith("https://api.the-odds-api.com/")) {
                sendResponse(requestId == null ? "" : requestId, 403, "Blocked host", "", "", "");
                return;
            }
            network.execute(() -> {
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(25000);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.setRequestProperty("User-Agent", "SureBetLive/5.0 Android");
                    int status = conn.getResponseCode();
                    InputStream stream = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();
                    StringBuilder body = new StringBuilder();
                    if (stream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) body.append(line).append('\n');
                        }
                    }
                    sendResponse(requestId, status, body.toString(), conn.getHeaderField("x-requests-remaining"), conn.getHeaderField("x-requests-used"), conn.getHeaderField("x-requests-last"));
                } catch (Exception e) {
                    sendResponse(requestId, 0, e.getClass().getSimpleName() + ": " + e.getMessage(), "", "", "");
                } finally {
                    if (conn != null) conn.disconnect();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        network.shutdownNow();
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
