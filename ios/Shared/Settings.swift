import SwiftUI

enum Settings {
    static let slotKey = "yubiKeySlot"
    static let numericSaltKey = "numericSaltKeyboard"
    static let clipboardTimeoutKey = "clipboardTimeoutSeconds"

    static let defaultSlot = YubiKeySlot.two
    static let defaultClipboardTimeout = 60
}

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @AppStorage(Settings.slotKey) private var slot = Settings.defaultSlot.rawValue
    @AppStorage(Settings.numericSaltKey) private var numericSalt = true
    @AppStorage(Settings.clipboardTimeoutKey) private var clipboardTimeout = Settings.defaultClipboardTimeout

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker("Slot", selection: $slot) {
                        ForEach(YubiKeySlot.allCases) { Text("Slot \($0.rawValue)").tag($0.rawValue) }
                    }
                } header: {
                    Text("YubiKey")
                } footer: {
                    Text("The slot programmed with HMAC-SHA1 challenge-response.")
                }

                Section {
                    Toggle("Numeric keypad for salt", isOn: $numericSalt)
                } footer: {
                    Text("Turn off if your salt contains letters or symbols.")
                }

                Section("Clipboard") {
                    Picker("Clear copied password", selection: $clipboardTimeout) {
                        Text("After 30 seconds").tag(30)
                        Text("After 1 minute").tag(60)
                        Text("After 5 minutes").tag(300)
                        Text("Never").tag(0)
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
