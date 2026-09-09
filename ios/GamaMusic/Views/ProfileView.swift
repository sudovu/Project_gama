import SwiftUI

struct ProfileView: View {
    let developerName = "VHUWON MATHERS"
    let developerRole = "Lead Architect & Systems Engineer"
    let githubUrl = "https://github.com/sudovu/Project_gama"

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header Card
                VStack(spacing: 16) {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [Color(red: 0, green: 1, blue: 0.8), Color(red: 0.66, green: 0.33, blue: 0.97)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 124, height: 124)

                        // Load developer profile photo
                        if let imagePath = Bundle.main.path(forResource: "img_vhuwon_profile", ofType: "jpg"),
                           let uiImage = UIImage(contentsOfFile: imagePath) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 116, height: 116)
                                .clipShape(Circle())
                        } else {
                            Image(systemName: "person.crop.circle.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 116, height: 116)
                                .foregroundColor(.gray)
                        }
                    }
                    .shadow(color: Color(red: 0, green: 1, blue: 0.8).opacity(0.3), radius: 12)

                    VStack(spacing: 4) {
                        HStack(spacing: 6) {
                            Text(developerName)
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)

                            Image(systemName: "checkmark.seal.fill")
                                .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                        }

                        Text(developerRole)
                            .font(.system(size: 13, weight: .medium, design: .monospaced))
                            .foregroundColor(Color(red: 0.66, green: 0.33, blue: 0.97))

                        Text("GAMA CORE SYSTEMS")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color(red: 0, green: 1, blue: 0.8).opacity(0.15))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                            .cornerRadius(12)
                            .padding(.top, 4)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                .cornerRadius(24)

                // Bio Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: "terminal.fill")
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                        Text("ARCHITECT DIRECTIVE")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(.gray)
                    }

                    Text("Developer & Systems Architect behind the GAMA Cybernetic Audio Network. Engineered to push frequency-tuned soundscapes, low-latency DSP pipelines, and multi-platform native music synchronization across Android, Windows, and iOS.")
                        .font(.system(size: 14))
                        .foregroundColor(Color(white: 0.85))
                        .lineSpacing(4)
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                .cornerRadius(20)

                // Platform Ecosystem Card
                VStack(alignment: .leading, spacing: 14) {
                    Text("ECOSYSTEM SYNCHRONIZATION")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(.gray)

                    HStack(spacing: 12) {
                        PlatformBadge(title: "Android", icon: "phone.fill", version: "Kotlin / Compose", active: true)
                        PlatformBadge(title: "Windows", icon: "laptopcomputer", version: "Electron / React", active: true)
                        PlatformBadge(title: "iOS", icon: "iphone", version: "SwiftUI Native", active: true)
                    }
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                .cornerRadius(20)

                // Repository & Links Card
                VStack(spacing: 12) {
                    if let url = URL(string: githubUrl) {
                        Link(destination: url) {
                            HStack {
                                Image(systemName: "link.circle.fill")
                                    .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                                Text("GitHub Repository")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.white)
                                Spacer()
                                Image(systemName: "arrow.up.right")
                                    .foregroundColor(.gray)
                            }
                            .padding()
                            .background(Color(red: 28/255, green: 28/255, blue: 40/255))
                            .cornerRadius(14)
                        }
                    }

                    HStack {
                        Text("Engine Version")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Spacer()
                        Text("GAMA iOS v1.0.0 (Unified)")
                            .font(.system(size: 12, weight: .bold, design: .monospaced))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                    }
                    .padding(.horizontal, 4)
                }
                .padding(20)
                .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                .cornerRadius(20)
            }
            .padding()
            .padding(.bottom, 90) // space for mini player
        }
        .background(Color(red: 10/255, green: 10/255, blue: 15/255).ignoresSafeArea())
    }
}

struct PlatformBadge: View {
    let title: String
    let icon: String
    let version: String
    let active: Bool

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(active ? Color(red: 0, green: 1, blue: 0.8) : .gray)

            Text(title)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.white)

            Text(version)
                .font(.system(size: 9, weight: .medium, design: .monospaced))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .padding(.horizontal, 8)
        .background(Color(red: 28/255, green: 28/255, blue: 40/255))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(active ? Color(red: 0, green: 1, blue: 0.8).opacity(0.3) : Color.clear, lineWidth: 1)
        )
    }
}
