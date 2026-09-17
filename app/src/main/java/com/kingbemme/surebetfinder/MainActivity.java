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

    private static final String UI_GZIP_BASE64 = "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";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.addJavascriptInterface(new NativeBridge(), "Native");
        webView.setWebViewClient(new WebViewClient());
        loadBundledUi();
    }

    private void loadBundledUi() {
        try {
            byte[] gz = Base64.decode(UI_GZIP_BASE64, Base64.DEFAULT);
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz));
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                String html = out.toString(StandardCharsets.UTF_8.name());
                webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            }
        } catch (Exception e) {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private void sendResponse(String requestId, int status, String body, String remaining, String used, String last) {
        final String js = "window.__nativeResponse(" +
                JSONObject.quote(requestId) + "," + status + "," +
                JSONObject.quote(body == null ? "" : body) + "," +
                JSONObject.quote(remaining == null ? "" : remaining) + "," +
                JSONObject.quote(used == null ? "" : used) + "," +
                JSONObject.quote(last == null ? "" : last) + ");";
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
                    conn.setRequestProperty("User-Agent", "SureBetLive/4.0 Android");

                    int status = conn.getResponseCode();
                    InputStream stream = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();
                    StringBuilder body = new StringBuilder();
                    if (stream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) body.append(line).append('\n');
                        }
                    }
                    sendResponse(requestId, status, body.toString(),
                            conn.getHeaderField("x-requests-remaining"),
                            conn.getHeaderField("x-requests-used"),
                            conn.getHeaderField("x-requests-last"));
                } catch (Exception e) {
                    sendResponse(requestId, 0, e.getClass().getSimpleName() + ": " + e.getMessage(), "", "", "");
                } finally {
                    if (conn != null) conn.disconnect();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        network.shutdownNow();
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
