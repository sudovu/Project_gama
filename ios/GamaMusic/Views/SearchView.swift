import SwiftUI

struct SearchView: View {
    @ObservedObject var audio = AudioManager.shared
    @State private var query: String = ""

    let quickFilters = ["Linkin Park", "Metallica", "Cyberpunk", "Daft Punk", "Eminem", "432Hz"]

    var searchResults: [GamaTrack] {
        if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return audio.queue
        }
        let q = query.lowercased()
        return audio.queue.filter {
            $0.title.lowercased().contains(q) ||
            $0.artist.lowercased().contains(q) ||
            $0.album.lowercased().contains(q) ||
            $0.genre.lowercased().contains(q)
        }
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("FREQUENCY SCANNER")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                        Text("Search Matrix")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.white)
                    }
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 8)

                // Search Bar Input
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                    TextField("Search artists, frequencies, tracks...", text: $query)
                        .foregroundColor(.white)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)

                    if !query.isEmpty {
                        Button(action: { query = "" }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.gray)
                        }
                    }
                }
                .padding()
                .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                .cornerRadius(16)
                .padding(.horizontal)

                // Quick Filter Tag Pills
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(quickFilters, id: \.self) { tag in
                            Button(action: {
                                query = tag
                            }) {
                                Text(tag)
                                    .font(.system(size: 12, weight: .medium))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(query == tag ? Color(red: 0, green: 1, blue: 0.8) : Color(red: 28/255, green: 28/255, blue: 40/255))
                                    .foregroundColor(query == tag ? .black : .white)
                                    .cornerRadius(14)
                            }
                        }
                    }
                    .padding(.horizontal)
                }

                // Results Header
                HStack {
                    Text("MATCHING FREQUENCIES (\(searchResults.count))")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(.gray)
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 4)

                // Search Results List
                ScrollView {
                    LazyVStack(spacing: 4) {
                        ForEach(searchResults) { track in
                            TrackRowView(track: track)
                        }
                    }
                    .padding(.bottom, 90)
                }
            }
            .background(Color(red: 10/255, green: 10/255, blue: 15/255).ignoresSafeArea())
            .navigationBarHidden(true)
        }
    }
}
