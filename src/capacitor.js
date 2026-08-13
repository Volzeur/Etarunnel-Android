// Capacitor bridge for Etarunnel Android
// This file handles communication between the web layer and native Android layer

import { App } from '@capacitor/app';
import { Browser } from '@capacitor/browser';

/**
 * Initialize Capacitor plugins and event listeners
 */
export function initCapacitor() {
  // Handle app lifecycle events
  App.addListener('appStateChange', ({ isActive }) => {
    if (isActive) {
      console.log('App is active');
      // Resume WebView when app becomes active
      window.postMessage({ type: 'APP_RESUMED' }, '*');
    } else {
      console.log('App is backgrounded');
      // Pause WebView when app goes to background
      window.postMessage({ type: 'APP_PAUSED' }, '*');
    }
  });

  // Handle back button on Android
  App.addListener('backButton', () => {
    console.log('Back button pressed');
    // Could implement custom back navigation here
  });

  console.log('Capacitor initialized');
}

/**
 * Open external URL in system browser
 * @param {string} url - The URL to open
 */
export async function openExternalUrl(url) {
  try {
    await Browser.open({ url });
    console.log(`Opened external URL: ${url}`);
  } catch (error) {
    console.error('Failed to open external URL:', error);
  }
}

/**
 * Check if URL is a YouTube domain
 * @param {string} url - The URL to check
 * @returns {boolean} - True if YouTube domain
 */
export function isYouTubeDomain(url) {
  const youtubeDomains = [
    'youtube.com',
    'www.youtube.com',
    'music.youtube.com',
    'youtu.be',
    'youtube-nocookie.com'
  ];
  
  try {
    const urlObj = new URL(url);
    return youtubeDomains.some(domain => 
      urlObj.hostname === domain || urlObj.hostname.endsWith('.' + domain)
    );
  } catch (e) {
    return false;
  }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  initCapacitor();
});

// Export for use in other modules
window.CapacitorBridge = {
  openExternalUrl,
  isYouTubeDomain,
  initCapacitor
};
