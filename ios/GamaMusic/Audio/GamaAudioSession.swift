import Foundation
import AVFoundation

/// Handles background audio session management for GAMA Music
public final class GamaAudioSession: NSObject {
    public static let shared = GamaAudioSession()

    private override init() {
        super.init()
        setupAudioSession()
        setupNotifications()
    }

    public func setupAudioSession() {
        do {
            let session = AVAudioSession.sharedInstance()
            // .playback category ensures audio continues playing when screen is locked or silent switch is toggled
            try session.setCategory(.playback, mode: .moviePlayback, options: [.allowAirPlay, .allowBluetooth, .allowBluetoothA2DP])
            try session.setActive(true)
            print("[GamaAudioSession] Audio session initialized in .playback mode successfully.")
        } catch {
            print("[GamaAudioSession] Failed to configure AVAudioSession: \(error.localizedDescription)")
        }
    }

    private func setupNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleInterruption),
            name: AVAudioSession.interruptionNotification,
            object: AVAudioSession.sharedInstance()
        )
    }

    @objc private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }

        switch type {
        case .began:
            print("[GamaAudioSession] Audio interrupted (call or other media).")
        case .ended:
            guard let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt else { return }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
                print("[GamaAudioSession] Interruption ended, resuming playback.")
                try? AVAudioSession.sharedInstance().setActive(true)
            }
        @unknown default:
            break
        }
    }
}
