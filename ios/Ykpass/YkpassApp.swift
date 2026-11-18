import SwiftUI

@main
struct YkpassApp: App {
    @StateObject private var model = GeneratorModel()
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            GeneratorView(model: model, mode: .standalone)
        }
        .onChange(of: scenePhase) { phase in
            // Don't leave a generated password on screen (or in the app switcher snapshot).
            if phase != .active { model.clearResult() }
        }
    }
}
