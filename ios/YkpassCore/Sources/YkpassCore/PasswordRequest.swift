import Foundation

/// A fully-resolved password generation request.
public struct PasswordRequest: Equatable, Sendable {
    public var salt: String
    public var passwordID: String
    public var flavor: PasswordFlavor

    public init(salt: String, passwordID: String, flavor: PasswordFlavor) {
        self.salt = salt
        self.passwordID = passwordID
        self.flavor = flavor
    }

    /// Builds a request from user input. Like the desktop clients, a trailing flavor suffix on the
    /// password ID (`!`, `@`, `#`, `$`) is stripped and overrides `selectedFlavor`.
    public static func parse(salt: String, passwordID rawID: String, selectedFlavor: PasswordFlavor) throws -> PasswordRequest {
        var passwordID = rawID.trimmingCharacters(in: .whitespacesAndNewlines)
        var flavor = selectedFlavor
        if let last = passwordID.last, let suffixFlavor = PasswordFlavor.flavor(forSuffix: last) {
            flavor = suffixFlavor
            passwordID.removeLast()
        }

        guard !salt.isEmpty else { throw YkpassError.emptySalt }
        guard !passwordID.isEmpty else { throw YkpassError.emptyPasswordID }
        return PasswordRequest(salt: salt, passwordID: passwordID, flavor: flavor)
    }

    /// The challenge sent to the YubiKey: `"<salt>,<password ID>"` as UTF-8.
    public var challenge: Data {
        Data("\(salt),\(passwordID)".utf8)
    }
}

public enum PasswordID {
    /// Derives a password ID from a domain or URL, exactly like the Android autofill service:
    /// strips `.co.`, `.gov.` and `www.`, then takes the second-level label
    /// (`accounts.google.com` → `google`).
    public static func fromServiceIdentifier(_ identifier: String) -> String {
        var host = identifier.trimmingCharacters(in: .whitespacesAndNewlines)
        if host.contains("://"), let url = URL(string: host), let urlHost = url.host, !urlHost.isEmpty {
            host = urlHost
        }

        let clean = host.replacingOccurrences(of: #"\.co\.|\.gov\.|www\."#, with: "", options: .regularExpression)

        // Java's String.split drops trailing empty strings; mirror that.
        var parts = clean.split(separator: ".", omittingEmptySubsequences: false).map(String.init)
        while parts.count > 1, parts.last?.isEmpty == true { parts.removeLast() }

        return parts.count > 1 ? parts[parts.count - 2] : (parts.last ?? clean)
    }
}
