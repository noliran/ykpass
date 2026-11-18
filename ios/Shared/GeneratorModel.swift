import Foundation
import SwiftUI
import UIKit
import UniformTypeIdentifiers
import YkpassCore

@MainActor
final class GeneratorModel: ObservableObject {
    enum Status: Equatable {
        case idle
        case waiting(String)
        case failed(String)
    }

    @Published var salt = ""
    @Published var passwordID = ""
    @Published var flavor: PasswordFlavor = .ascii85
    @Published private(set) var result = ""
    @Published private(set) var status: Status = .idle

    @AppStorage(Settings.slotKey) private var slotRawValue = Settings.defaultSlot.rawValue
    @AppStorage(Settings.clipboardTimeoutKey) private var clipboardTimeout = Settings.defaultClipboardTimeout

    private let yubiKey: YubiKeyService

    init(passwordID: String = "", yubiKey: YubiKeyService = .shared) {
        self.passwordID = passwordID
        self.yubiKey = yubiKey
    }

    var isWaiting: Bool {
        if case .waiting = status { return true }
        return false
    }

    var canGenerate: Bool {
        !salt.isEmpty && !passwordID.trimmingCharacters(in: .whitespaces).isEmpty && !isWaiting
    }

    /// Asks the YubiKey for a response and turns it into a password.
    /// Returns the password, or `nil` if it failed (the error is reflected in `status`).
    @discardableResult
    func generate() async -> String? {
        guard !isWaiting else { return nil }
        do {
            let request = try PasswordRequest.parse(salt: salt, passwordID: passwordID, selectedFlavor: flavor)
            // Reflect a suffix typed on the password ID (e.g. "github!") in the UI, like the desktop clients.
            passwordID = request.passwordID
            flavor = request.flavor
            result = ""

            status = .waiting(yubiKey.accessoryConnected ? "Touch your YubiKey…" : "Hold your YubiKey near the top of your device…")
            let slot = YubiKeySlot(rawValue: slotRawValue) ?? Settings.defaultSlot
            let response = try await yubiKey.challengeResponse(request.challenge, slot: slot)
            let password = try request.flavor.password(from: response)

            result = password
            status = .idle
            return password
        } catch YubiKeyError.cancelled {
            status = .idle
        } catch {
            status = .failed(error.localizedDescription)
        }
        return nil
    }

    func copyResult() {
        guard !result.isEmpty else { return }
        var options: [UIPasteboard.OptionsKey: Any] = [.localOnly: true]
        if clipboardTimeout > 0 {
            options[.expirationDate] = Date().addingTimeInterval(TimeInterval(clipboardTimeout))
        }
        UIPasteboard.general.setItems([[UTType.plainText.identifier: result]], options: options)
    }

    func clear() {
        passwordID = ""
        clearResult()
    }

    func clearResult() {
        result = ""
        if case .failed = status { status = .idle }
    }
}
