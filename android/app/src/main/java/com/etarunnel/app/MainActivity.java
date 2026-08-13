package com.etarunnel.app;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.util.Log;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * MainActivity for Etarunnel Android
 */
public class MainActivity extends BridgeActivity {

    private static final String TAG = "Etarunnel";

    // List of domains to block. 
    // Be specific to avoid blocking legitimate content.
    private static final String[] AD_DOMAINS = {
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "google-analytics.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "tpc.googlesyndication.com",
        "adsystem.google.com"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the bridge and set our custom WebViewClient
        getBridge().getWebView().setWebViewClient(new AdBlockWebViewClient(getBridge()));
        
        Log.d(TAG, "MainActivity initialized with AdBlocking");
    }

    private class AdBlockWebViewClient extends BridgeWebViewClient {

        public AdBlockWebViewClient(com.getcapacitor.Bridge bridge) {
            super(bridge);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) {
                return super.shouldInterceptRequest(view, request);
            }

            String url = request.getUrl().toString();

            // 1. ALWAYS ALLOW local Capacitor assets
            // If we block these, the app shows a blank screen or err_blocked_by_response
            if (url.startsWith("file://") || 
                url.startsWith("content://") || 
                url.startsWith("data:") || 
                url.startsWith("blob:")) {
                return super.shouldInterceptRequest(view, request);
            }

            // 2. Check against Ad Domains
            if (isAdDomain(url)) {
                Log.d(TAG, "Blocked Ad: " + url);
                // Return a proper empty response with 204 No Content status
                // This prevents err_blocked_by_response errors
                return new WebResourceResponse(
                    "text/plain", 
                    "UTF-8", 
                    204, 
                    "No Content", 
                    null, 
                    new ByteArrayInputStream("".getBytes())
                );
            }

            // 3. Allow everything else by calling super
            // This is critical: it lets Capacitor handle its own internal requests
            return super.shouldInterceptRequest(view, request);
        }

        private boolean isAdDomain(String url) {
            try {
                URL parsedUrl = new URL(url);
                String host = parsedUrl.getHost().toLowerCase();

                for (String domain : AD_DOMAINS) {
                    if (host.equals(domain) || host.endsWith("." + domain)) {
                        return true;
                    }
                }
            } catch (MalformedURLException e) {
                // If URL is invalid, don't block it just in case
                return false;
            }
            return false;
        }
    }
}
