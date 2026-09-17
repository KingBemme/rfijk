package com.kingbemme.surebetfinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private WebView webView;
    private final ExecutorService network = Executors.newSingleThreadExecutor();

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
        webView.loadUrl("file:///android_asset/index.html");
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
                    conn.setReadTimeout(20000);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("User-Agent", "SureBetLive/2.0 Android");

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
