import Foundation

/// Text encodings used to turn the YubiKey's HMAC-SHA1 response into a password.
/// They intentionally mirror the libraries/tools used by the other ykpass clients:
/// `ascii85 -n` / fzakaria Ascii85, `base64` / Guava, `base32` / Guava (RFC 4648, padded).
public enum Encoding {
    /// Adobe-style Ascii85 without `<~ ~>` delimiters. A full all-zero group is written as `z`.
    public static func ascii85(_ bytes: [UInt8]) -> String {
        var out = ""
        out.reserveCapacity(bytes.count * 5 / 4 + 5)

        var index = 0
        while index < bytes.count {
            let count = min(4, bytes.count - index)
            var value: UInt32 = 0
            for i in 0..<4 {
                value <<= 8
                if i < count { value |= UInt32(bytes[index + i]) }
            }

            if count == 4 && value == 0 {
                out.append("z")
            } else {
                var chars = [Character](repeating: "!", count: 5)
                var v = value
                for i in stride(from: 4, through: 0, by: -1) {
                    chars[i] = Character(Unicode.Scalar(UInt8(v % 85) + 33))
                    v /= 85
                }
                out.append(contentsOf: chars.prefix(count + 1))
            }
            index += count
        }
        return out
    }

    /// Standard Base64 (`A-Z a-z 0-9 + /`) with `=` padding.
    public static func base64(_ bytes: [UInt8]) -> String {
        Data(bytes).base64EncodedString()
    }

    private static let base32Alphabet = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567")

    /// RFC 4648 Base32 (`A-Z 2-7`) with `=` padding.
    public static func base32(_ bytes: [UInt8]) -> String {
        var out = ""
        var buffer: UInt32 = 0
        var bits = 0
        for byte in bytes {
            buffer = (buffer << 8) | UInt32(byte)
            bits += 8
            while bits >= 5 {
                out.append(base32Alphabet[Int((buffer >> UInt32(bits - 5)) & 0x1F)])
                bits -= 5
            }
            buffer &= (1 << UInt32(bits)) - 1
        }
        if bits > 0 {
            out.append(base32Alphabet[Int((buffer << UInt32(5 - bits)) & 0x1F)])
        }
        while out.count % 8 != 0 { out.append("=") }
        return out
    }
}
