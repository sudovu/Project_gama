import SwiftUI
import WebKit

/// Reusable WKWebView-backed YouTube audio/video playback engine
public struct YouTubePlayerView: UIViewRepresentable {
    public class Coordinator: NSObject, WKScriptMessageHandler, WKNavigationDelegate {
        var parent: YouTubePlayerView
        weak var webView: WKWebView?

        init(_ parent: YouTubePlayerView) {
            self.parent = parent
        }

        public func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            guard let dict = message.body as? [String: Any],
                  let event = dict["event"] as? String else {
                return
            }

            DispatchQueue.main.async {
                switch event {
                case "ready":
                    self.parent.onReady?()
                case "stateChange":
                    if let state = dict["state"] as? Int {
                        // YT.PlayerState: -1 unstarted, 0 ended, 1 playing, 2 paused, 3 buffering, 5 cued
                        self.parent.onStateChange?(state)
                    }
                case "timeUpdate":
                    if let time = dict["time"] as? Double,
                       let duration = dict["duration"] as? Double {
                        self.parent.onTimeUpdate?(time, duration)
                    }
                case "error":
                    if let code = dict["code"] as? Int {
                        print("[YouTubePlayerView] Error: \(code)")
                    }
                default:
                    break
                }
            }
        }

        public func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            // Player page loaded
        }
    }

    public let videoId: String
    public let isPlaying: Bool
    public var onReady: (() -> Void)?
    public var onStateChange: ((Int) -> Void)?
    public var onTimeUpdate: ((Double, Double) -> Void)?

    public func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    public func makeUIView(context: Context) -> WKWebView {
        let preferences = WKPreferences()
        preferences.javaScriptCanOpenWindowsAutomatically = false

        let configuration = WKWebViewConfiguration()
        configuration.preferences = preferences
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []

        let contentController = WKUserContentController()
        contentController.add(context.coordinator, name: "gamaBridge")
        configuration.userContentController = contentController

        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        context.coordinator.webView = webView

        let html = generateHTML(videoId: videoId)
        webView.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))

        return webView
    }

    public func updateUIView(_ uiView: WKWebView, context: Context) {
        context.coordinator.parent = self
    }

    private func generateHTML(videoId: String) -> String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
            html, body, #player { width: 100%; height: 100%; overflow: hidden; }
          </style>
          <script src="https://www.youtube.com/iframe_api"></script>
        </head>
        <body>
          <div id="player"></div>
          <script>
            var player;
            var timeInterval = null;

            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                height: '100%',
                width: '100%',
                videoId: '\(videoId)',
                playerVars: {
                  'autoplay': 1,
                  'playsinline': 1,
                  'controls': 0,
                  'modestbranding': 1,
                  'rel': 0,
                  'enablejsapi': 1,
                  'origin': 'https://www.youtube.com'
                },
                events: {
                  'onReady': onPlayerReady,
                  'onStateChange': onPlayerStateChange,
                  'onError': onPlayerError
                }
              });
            }

            function onPlayerReady(event) {
              window.webkit.messageHandlers.gamaBridge.postMessage({ event: 'ready' });
              startTimeUpdates();
            }

            function onPlayerStateChange(event) {
              window.webkit.messageHandlers.gamaBridge.postMessage({
                event: 'stateChange',
                state: event.data
              });
            }

            function onPlayerError(event) {
              window.webkit.messageHandlers.gamaBridge.postMessage({
                event: 'error',
                code: event.data
              });
            }

            function startTimeUpdates() {
              if (timeInterval) clearInterval(timeInterval);
              timeInterval = setInterval(function() {
                if (player && player.getCurrentTime && player.getDuration) {
                  window.webkit.messageHandlers.gamaBridge.postMessage({
                    event: 'timeUpdate',
                    time: player.getCurrentTime() || 0,
                    duration: player.getDuration() || 0
                  });
                }
              }, 500);
            }

            // External control functions
            function gamaPlay() { if (player && player.playVideo) player.playVideo(); }
            function gamaPause() { if (player && player.pauseVideo) player.pauseVideo(); }
            function gamaSeek(sec) { if (player && player.seekTo) player.seekTo(sec, true); }
            function gamaLoadVideo(id) { if (player && player.loadVideoById) player.loadVideoById(id); }
            function gamaSetVolume(vol) { if (player && player.setVolume) player.setVolume(vol); }
          </script>
        </body>
        </html>
        """
    }
}
