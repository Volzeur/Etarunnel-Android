// Main JavaScript for Etarunnel Android
// Handles toggle between YouTube and YouTube Music

const YOUTUBE_URL = 'https://www.youtube.com';
const YOUTUBE_MUSIC_URL = 'https://music.youtube.com';

// Custom CSS to inject into YouTube pages
const CUSTOM_CSS = `
  /* Change font to system sans-serif */
  body, ytd-app, tp-yt-app-drawer {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif !important;
  }
  
  /* Hide YouTube Shorts shelf */
  ytd-rich-shelf-renderer[is-shorts],
  ytd-reel-shelf-renderer,
  ytd-guide-entry-renderer[aria-label="Shorts"] {
    display: none !important;
  }
  
  /* Hide banner ads */
  ytd-banner-promo-renderer,
  ytd-promoted-sparkles-web-renderer,
  ytd-compact-promoted-video-renderer,
  ytd-promoted-video-renderer {
    display: none !important;
  }
  
  /* Hide video ads overlay */
  .video-ads,
  .ytp-ad-overlay-container,
  .ytp-ad-text-overlay,
  .ytp-ad-image-overlay {
    display: none !important;
  }
  
  /* Improve mobile layout */
  @media (max-width: 768px) {
    #masthead-container {
      position: sticky !important;
    }
    
    ytd-watch-flexy {
      --ytd-watch-flexy-max-player-width: 100% !important;
    }
  }
  
  /* Enable ambient mode styling */
  ytd-watch-flexy[ambient-mode] {
    --ytd-watch-ambient-background-color: rgba(0, 0, 0, 0.5);
  }
`;

// Custom JavaScript to inject into YouTube pages
const CUSTOM_JS = `
  // Enable ambient mode
  try {
    const app = document.querySelector('ytd-app');
    if (app && app.setAttribute) {
      app.setAttribute('ambient-mode', '');
    }
  } catch(e) {
    console.log('Ambient mode setup error:', e);
  }
  
  // Force highest quality video
  try {
    const player = document.querySelector('video');
    if (player) {
      player.playbackRate = 1.0;
    }
  } catch(e) {
    console.log('Player setup error:', e);
  }
  
  console.log('Etarunnel custom JS injected');
`;

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
  const modeToggle = document.getElementById('modeToggle');
  const refreshBtn = document.getElementById('refreshBtn');
  const youtubeFrame = document.getElementById('youtube-frame');
  
  // Load saved mode from localStorage
  const savedMode = localStorage.getItem('etarunnel_mode') || 'youtube';
  modeToggle.checked = savedMode === 'music';
  
  // Set initial URL
  updateWebView(modeToggle.checked);
  
  // Toggle event
  modeToggle.addEventListener('change', () => {
    updateWebView(modeToggle.checked);
    localStorage.setItem('etarunnel_mode', modeToggle.checked ? 'music' : 'youtube');
  });
  
  // Refresh event
  refreshBtn.addEventListener('click', () => {
    const currentUrl = youtubeFrame.src;
    if (currentUrl) {
      youtubeFrame.src = currentUrl;
    } else {
      updateWebView(modeToggle.checked);
    }
  });
  
  // Handle messages from Capacitor native layer
  window.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'CAPACITOR_MESSAGE') {
      console.log('Received from native:', event.data);
    }
  });
});

/**
 * Update the WebView with the appropriate URL
 * @param {boolean} isMusicMode - true for YouTube Music, false for YouTube
 */
function updateWebView(isMusicMode) {
  const youtubeFrame = document.getElementById('youtube-frame');
  const url = isMusicMode ? YOUTUBE_MUSIC_URL : YOUTUBE_URL;
  
  // In a real Capacitor app, we'd use the native WebView
  // For now, we'll use an iframe as a placeholder
  youtubeFrame.src = url;
  
  console.log(`Switched to ${isMusicMode ? 'YouTube Music' : 'YouTube'}`);
}

// Export functions for Capacitor bridge
window.EtarunnelApp = {
  toggleMode: (isMusic) => {
    const modeToggle = document.getElementById('modeToggle');
    modeToggle.checked = isMusic;
    updateWebView(isMusic);
    localStorage.setItem('etarunnel_mode', isMusic ? 'music' : 'youtube');
  },
  
  getCurrentMode: () => {
    return localStorage.getItem('etarunnel_mode') || 'youtube';
  },
  
  injectCSS: () => {
    const style = document.createElement('style');
    style.textContent = CUSTOM_CSS;
    document.head.appendChild(style);
  },
  
  injectJS: () => {
    const script = document.createElement('script');
    script.textContent = CUSTOM_JS;
    document.body.appendChild(script);
  }
};
