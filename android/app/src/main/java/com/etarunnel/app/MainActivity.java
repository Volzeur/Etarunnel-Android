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

    // List of specific ad-related URL patterns to block
    // More selective to avoid breaking YouTube functionality
    private static final String[] AD_PATTERNS = {
        "/pagead/",
        "/ads/",
        "/ad/",
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com/pagead",
        "google-analytics.com",
        "/api/ads/",
        "adsystem.google.com"
    };
    
    // Domains that should NEVER be blocked (YouTube core functionality)
    private static final String[] ALLOWED_DOMAINS = {
        "youtube.com",
        "youtu.be",
        "googlevideo.com",
        "ytimg.com",
        "ggpht.com",
        "googleapis.com/youtube",
        "wide-youtube.l.google.com"
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
            if (url.startsWith("file://") || 
                url.startsWith("content://") || 
                url.startsWith("data:") || 
                url.startsWith("blob:")) {
                return super.shouldInterceptRequest(view, request);
            }

            // 2. ALWAYS ALLOW YouTube core domains - never block these
            if (isAllowedDomain(url)) {
                return super.shouldInterceptRequest(view, request);
            }

            // 3. Check against Ad Patterns
            if (isAdPattern(url)) {
                Log.d(TAG, "Blocked Ad: " + url);
                // Return a proper empty response with 204 No Content status
                return new WebResourceResponse(
                    "text/plain", 
                    "UTF-8", 
                    204, 
                    "No Content", 
                    null, 
                    new ByteArrayInputStream("".getBytes())
                );
            }

            // 4. Allow everything else by calling super
            return super.shouldInterceptRequest(view, request);
        }

        private boolean isAdPattern(String url) {
            String urlLower = url.toLowerCase();
            
            for (String pattern : AD_PATTERNS) {
                if (urlLower.contains(pattern)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isAllowedDomain(String url) {
            try {
                URL parsedUrl = new URL(url);
                String host = parsedUrl.getHost().toLowerCase();

                for (String domain : ALLOWED_DOMAINS) {
                    if (host.equals(domain) || host.endsWith("." + domain)) {
                        return true;
                    }
                }
            } catch (MalformedURLException e) {
                return false;
            }
            return false;
        }
    }
}
