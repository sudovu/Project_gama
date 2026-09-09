import SwiftUI

struct EqualizerView: View {
    @ObservedObject var audio = AudioManager.shared
    @Environment(\.dismiss) private var dismiss

    let bandFrequencies = ["60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz"]

    var body: some View {
        NavigationView {
            ZStack {
                Color(red: 10/255, green: 10/255, blue: 15/255)
                    .ignoresSafeArea()

                VStack(spacing: 24) {
                    // Header Status
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("5-BAND HARDWARE DSP")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                            Text("Cyber Frequency Equalizer")
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Spacer()
                        Button(action: { dismiss() }) {
                            Image(systemName: "xmark.circle.fill")
                                .font(.title2)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.horizontal)

                    // 432Hz Harmonic Toggle
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("432Hz Harmonic Resonance")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                            Text("Mathematical natural frequency alignment")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                        Spacer()
                        Toggle("", isOn: $audio.is432HzHarmonic)
                            .labelsHidden()
                            .tint(Color(red: 0, green: 1, blue: 0.8))
                    }
                    .padding()
                    .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                    .cornerRadius(16)
                    .padding(.horizontal)

                    // Preset Selector
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(EqualizerPreset.presets) { preset in
                                Button(action: {
                                    audio.applyPreset(preset)
                                }) {
                                    Text(preset.name)
                                        .font(.system(size: 13, weight: .semibold))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(
                                            audio.currentPreset == preset.name
                                                ? Color(red: 0, green: 1, blue: 0.8)
                                                : Color(red: 28/255, green: 28/255, blue: 40/255)
                                        )
                                        .foregroundColor(audio.currentPreset == preset.name ? .black : .white)
                                        .cornerRadius(20)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // Sliders
                    HStack(spacing: 18) {
                        ForEach(0..<5, id: \.self) { i in
                            VStack(spacing: 12) {
                                Text("\(Int(audio.eqGains[i])) dB")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(audio.eqGains[i] > 0 ? Color(red: 0, green: 1, blue: 0.8) : .gray)

                                // Slider representation
                                Slider(
                                    value: $audio.eqGains[i],
                                    in: -12...12,
                                    step: 0.5
                                )
                                .accentColor(Color(red: 0, green: 1, blue: 0.8))
                                .rotationEffect(.degrees(-90))
                                .frame(width: 140, height: 40)
                                .padding(.vertical, 50)

                                Text(bandFrequencies[i])
                                    .font(.system(size: 11, weight: .semibold, design: .monospaced))
                                    .foregroundColor(.white)
                            }
                        }
                    }
                    .padding(.vertical, 10)
                    .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                    .cornerRadius(20)
                    .padding(.horizontal)

                    Spacer()
                }
                .padding(.top)
            }
            .navigationBarHidden(true)
        }
    }
}
