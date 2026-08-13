package com.etarunnel.app;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.util.Log;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * MainActivity for Etarunnel Android
 * Extends BridgeActivity to integrate with Capacitor
 * Implements ad-blocking by overriding shouldInterceptRequest
 */
public class MainActivity extends BridgeActivity {
    
    private static final String TAG = "Etarunnel";
    
    // List of domains to block
    private final List<String> AD_DOMAINS = Arrays.asList(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "google-analytics.com",
        "ad.doubleclick.net",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "tpc.googlesyndication.com",
        "youtube-nocookie.com",
        "adsystem.google.com",
        "g.doubleclick.net"
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "MainActivity created - Initializing AdBlocker");
        
        // Set the custom WebViewClient after the bridge is initialized
        // We wait for the bridge to be ready
        getBridge().getWebView().setWebViewClient(new AdBlockWebViewClient());
    }

    /**
     * Custom WebViewClient that handles ad blocking
     */
    private class AdBlockWebViewClient extends BridgeWebViewClient {
        
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) {
                return super.shouldInterceptRequest(view, request);
            }

            String url = request.getUrl().toString();

            // Check if the URL matches any ad domain
            if (isAdUrl(url)) {
                Log.d(TAG, "Blocking ad request: " + url);
                
                // Return an empty response to block the request
                // Returning a valid empty response is safer than returning null
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
            }

            // IMPORTANT: For all other requests (app assets, APIs, legitimate content),
            // we MUST call super to allow the load to proceed normally.
            return super.shouldInterceptRequest(view, request);
        }

        /**
         * Checks if a URL belongs to a known ad domain
         */
        private boolean isAdUrl(String url) {
            if (url == null || url.isEmpty()) {
                return false;
            }

            String lowerUrl = url.toLowerCase();

            // Don't block local assets or data URIs
            if (lowerUrl.startsWith("file://") || 
                lowerUrl.startsWith("data:") || 
                lowerUrl.startsWith("blob:") ||
                lowerUrl.contains("localhost") ||
                lowerUrl.contains("127.0.0.1")) {
                return false;
            }

            // Check against ad domains
            for (String domain : AD_DOMAINS) {
                if (lowerUrl.contains(domain)) {
                    return true;
                }
            }

            return false;
        }
    }
}
