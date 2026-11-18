import Foundation

/// A visual fingerprint of the salt, so a typo can be spotted before generating a password.
/// Computed exactly like the Android app: CRC32 of the salt picks a background color and an icon.
public struct SaltIndicator: Equatable, Sendable {
    /// Background color as 0xRRGGBB.
    public let backgroundRGB: UInt32
    /// Foreground (icon) color as 0xRRGGBB: the inverse of the background.
    public let foregroundRGB: UInt32
    /// Index into `FontAwesomeIcons.codePoints`.
    public let iconIndex: Int

    public init(salt: String) {
        let crc = CRC32.checksum(Array(salt.utf8))
        // Java: Math.abs((int) crc). abs(Int.MIN_VALUE) overflows in Java; avoid crashing here.
        let signed = Int32(bitPattern: crc)
        let magnitude = Int(signed.magnitude)

        backgroundRGB = UInt32(truncatingIfNeeded: magnitude) & 0xFFFFFF
        foregroundRGB = 0xFFFFFF - backgroundRGB
        iconIndex = magnitude % FontAwesomeIcons.codePoints.count
    }

    public var iconCodePoint: UInt32 { FontAwesomeIcons.codePoints[iconIndex] }

    public var icon: String {
        String(Character(Unicode.Scalar(iconCodePoint)!))
    }
}

/// CRC-32 (IEEE 802.3, as used by zlib and java.util.zip.CRC32).
public enum CRC32 {
    private static let table: [UInt32] = (0..<256).map { i -> UInt32 in
        var c = UInt32(i)
        for _ in 0..<8 {
            c = (c & 1) != 0 ? 0xEDB88320 ^ (c >> 1) : c >> 1
        }
        return c
    }

    public static func checksum(_ bytes: [UInt8]) -> UInt32 {
        var crc: UInt32 = 0xFFFFFFFF
        for byte in bytes {
            crc = table[Int((crc ^ UInt32(byte)) & 0xFF)] ^ (crc >> 8)
        }
        return crc ^ 0xFFFFFFFF
    }
}
