package com.etarunnel.app;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.util.Log;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

/**
 * MainActivity for Etarunnel Android
 * Extends BridgeActivity to integrate with Capacitor
 * Implements ad-blocking by overriding shouldInterceptRequest
 */
public class MainActivity extends BridgeActivity {
    
    // Tag for logging
    private static final String TAG = "Etarunnel";
    
    // Ad domains to block
    private static final String[] AD_DOMAINS = {
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "google-analytics.com",
        "ad.doubleclick.net",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "tpc.googlesyndication.com",
        "youtube-nocookie.com"
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the Capacitor bridge
        // Note: registerPlugin() calls are handled automatically by Capacitor
        
        Log.d(TAG, "MainActivity created - Etarunnel starting");
        
        // Set up ad-blocking after the WebView is initialized
        getBridge().getWebView().setWebViewClient(new AdBlockWebViewClient(getBridge()));
    }
    
    /**
     * Inner class implementing ad-blocking logic
     * Overrides shouldInterceptRequest to block known ad domains
     */
    private class AdBlockWebViewClient extends BridgeWebViewClient {
        
        public AdBlockWebViewClient(com.getcapacitor.Bridge bridge) {
            super(bridge);
        }
        
        /**
         * Intercept web resource requests and block ads
         * @param view The WebView making the request
         * @param request The WebResourceRequest containing URL info
         * @return WebResourceResponse (null to allow, empty response to block)
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) {
                return super.shouldInterceptRequest(view, request);
            }
            
            String url = request.getUrl().toString();
            
            // Check if this request should be blocked
            if (shouldBlockAd(url)) {
                Log.d(TAG, "Blocked ad request: " + url);
                
                // Return empty response to block the ad
                // Using text/plain with 200 OK but empty content
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
            
            // Allow the request
            return super.shouldInterceptRequest(view, request);
        }
        
        /**
         * Legacy method for older Android versions (API < 21)
         * @param view The WebView making the request
         * @param url The URL string being requested
         * @return WebResourceResponse (null to allow, empty response to block)
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url == null) {
                return super.shouldInterceptRequest(view, url);
            }
            
            // Check if this request should be blocked
            if (shouldBlockAd(url)) {
                Log.d(TAG, "Blocked ad request (legacy): " + url);
                
                // Return empty response to block the ad
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
            
            // Allow the request
            return super.shouldInterceptRequest(view, url);
        }
        
        /**
         * Check if a URL matches known ad domains
         * @param url The URL to check
         * @return true if the URL should be blocked as an ad
         */
        private boolean shouldBlockAd(String url) {
            if (url == null || url.isEmpty()) {
                return false;
            }
            
            // Convert to lowercase for case-insensitive matching
            String lowerUrl = url.toLowerCase();
            
            // Check against all known ad domains
            for (String domain : AD_DOMAINS) {
                if (lowerUrl.contains(domain)) {
                    return true;
                }
            }
            
            // Also block common ad-related paths
            if (lowerUrl.contains("/ads/") || 
                lowerUrl.contains("/ad/") ||
                lowerUrl.contains("sponsor") ||
                lowerUrl.contains("promoted")) {
                return true;
            }
            
            return false;
        }
    }
}
