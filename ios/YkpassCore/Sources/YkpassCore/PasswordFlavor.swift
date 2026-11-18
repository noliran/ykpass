import Foundation

/// The password "flavors" supported by ykpass. On the desktop clients the flavor is selected
/// with a suffix on the password ID (e.g. `github!`); on mobile it can also be picked explicitly.
public enum PasswordFlavor: String, CaseIterable, Identifiable, Sendable {
    case ascii85
    case base64
    case base32Long
    case base32Short
    case base32Mixed

    public var id: String { rawValue }

    /// The suffix that selects this flavor on the desktop clients (`nil` for the default).
    public var suffix: Character? {
        switch self {
        case .ascii85: return nil
        case .base64: return "!"
        case .base32Long: return "@"
        case .base32Short: return "#"
        case .base32Mixed: return "$"
        }
    }

    /// Short label, matching the Android radio buttons.
    public var label: String {
        suffix.map(String.init) ?? "Default"
    }

    /// Description, matching the Android tooltips.
    public var detail: String {
        switch self {
        case .ascii85: return "B85 (16)"
        case .base64: return "B64 (16)"
        case .base32Long: return "B32 (12)"
        case .base32Short: return "B32 (8)"
        case .base32Mixed: return "B32 (8 mixed-case)"
        }
    }

    public var length: Int {
        switch self {
        case .ascii85, .base64: return 16
        case .base32Long: return 12
        case .base32Short, .base32Mixed: return 8
        }
    }

    /// Turns a challenge-response result into a password.
    public func password(from response: [UInt8]) throws -> String {
        let encoded: String
        switch self {
        case .ascii85: encoded = Encoding.ascii85(response)
        case .base64: encoded = Encoding.base64(response)
        case .base32Long, .base32Short, .base32Mixed: encoded = Encoding.base32(response)
        }

        guard encoded.count >= length else {
            throw YkpassError.responseTooShort(got: encoded.count, expected: length)
        }
        let password = String(encoded.prefix(length))

        if self == .base32Mixed {
            return String(password.prefix(4)) + password.dropFirst(4).lowercased()
        }
        return password
    }

    public func password(from response: Data) throws -> String {
        try password(from: [UInt8](response))
    }

    /// Finds the flavor selected by a trailing suffix character, if any.
    public static func flavor(forSuffix suffix: Character) -> PasswordFlavor? {
        allCases.first { $0.suffix == suffix }
    }
}

public enum YkpassError: LocalizedError, Equatable {
    case responseTooShort(got: Int, expected: Int)
    case emptySalt
    case emptyPasswordID

    public var errorDescription: String? {
        switch self {
        case let .responseTooShort(got, expected):
            return "Length assertion: got \(got) characters but expected \(expected)"
        case .emptySalt:
            return "Salt is empty"
        case .emptyPasswordID:
            return "Password ID is empty"
        }
    }
}
