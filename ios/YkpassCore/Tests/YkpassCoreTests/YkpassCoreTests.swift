import XCTest
@testable import YkpassCore

/// Reference values were generated with Python (`hmac`, `base64.a85encode`, `base64.b32encode`,
/// `zlib.crc32`) and the Android icon list, to guarantee iOS produces the same passwords as the
/// other ykpass clients. Responses simulate a YubiKey with secret 00112233445566778899aabbccddeeff00112233.
final class YkpassCoreTests: XCTestCase {
    private func bytes(_ hex: String) -> [UInt8] {
        var out = [UInt8]()
        var it = hex.makeIterator()
        while let a = it.next(), let b = it.next() {
            out.append(UInt8(String([a, b]), radix: 16)!)
        }
        return out
    }

    func testFlavors() throws {
        let vectors: [(String, [PasswordFlavor: String])] = [
            ("26cbcd71ebc65d4cb58a394073c983ab9a478098", [
                .ascii85: "-HjYRlc*4([>Ht0F", .base64: "JsvNcevGXUy1ijlA",
                .base32Long: "E3F424PLYZOU", .base32Short: "E3F424PL", .base32Mixed: "E3F424pl",
            ]),
            ("2525a40364ad189c8764fa5752d97dc7c7366d5c", [
                .ascii85: ",pf>hA??]=LLg6_;", .base64: "JSWkA2StGJyHZPpX",
                .base32Long: "EUS2IA3EVUMJ", .base32Short: "EUS2IA3E", .base32Mixed: "EUS2ia3e",
            ]),
            ("0c6ee3715ae1b49b2fc049651b93b2777e80d492", [
                .ascii85: "$uY*F>2ciX0>L.j)", .base64: "DG7jcVrhtJsvwEll",
                .base32Long: "BRXOG4K24G2J", .base32Short: "BRXOG4K2", .base32Mixed: "BRXOg4k2",
            ]),
        ]
        for (hex, expected) in vectors {
            for (flavor, password) in expected {
                XCTAssertEqual(try flavor.password(from: bytes(hex)), password, "\(flavor) for \(hex)")
            }
        }
    }

    func testAscii85ZeroGroupAndPartial() {
        XCTAssertEqual(Encoding.ascii85([0, 0, 0, 0, 1, 2, 3, 4, 5]), #"z!<N?+"T"#)
        XCTAssertEqual(Encoding.ascii85([]), "")
    }

    func testBase32Padding() {
        XCTAssertEqual(Encoding.base32(Array("f".utf8)), "MY======")
        XCTAssertEqual(Encoding.base32(Array("foobar".utf8)), "MZXW6YTBOI======")
    }

    func testResponseTooShort() {
        XCTAssertThrowsError(try PasswordFlavor.ascii85.password(from: [1, 2, 3]))
    }

    func testChallengeAndSuffixParsing() throws {
        let plain = try PasswordRequest.parse(salt: "1234", passwordID: "google", selectedFlavor: .base64)
        XCTAssertEqual(plain.flavor, .base64)
        XCTAssertEqual(plain.challenge, Data("1234,google".utf8))

        let suffixed = try PasswordRequest.parse(salt: "1234", passwordID: " github# ", selectedFlavor: .ascii85)
        XCTAssertEqual(suffixed.passwordID, "github")
        XCTAssertEqual(suffixed.flavor, .base32Short)

        XCTAssertThrowsError(try PasswordRequest.parse(salt: "", passwordID: "x", selectedFlavor: .ascii85))
        XCTAssertThrowsError(try PasswordRequest.parse(salt: "1", passwordID: "!", selectedFlavor: .ascii85))
    }

    func testPasswordIDFromDomain() {
        XCTAssertEqual(PasswordID.fromServiceIdentifier("accounts.google.com"), "google")
        XCTAssertEqual(PasswordID.fromServiceIdentifier("www.github.com"), "github")
        XCTAssertEqual(PasswordID.fromServiceIdentifier("https://login.example.org/path?q=1"), "example")
        XCTAssertEqual(PasswordID.fromServiceIdentifier("bbc.co.uk"), "bbcuk") // same quirk as Android
        XCTAssertEqual(PasswordID.fromServiceIdentifier("localhost"), "localhost")
        XCTAssertEqual(PasswordID.fromServiceIdentifier("example.com."), "example")
    }

    func testSaltIndicatorMatchesAndroid() {
        let cases: [(String, UInt32, Int, UInt32)] = [
            ("", 0x000000, 0, 0xF000),
            ("1234", 0x1C1F5D, 741, 0xF5CE),
            ("0000", 0x9BC472, 842, 0xF70C),
            ("98765", 0xF8A419, 469, 0xF3FD),
            ("hello", 0x10A686, 422, 0xF2D2),
        ]
        XCTAssertEqual(FontAwesomeIcons.codePoints.count, 956)
        for (salt, rgb, index, codePoint) in cases {
            let indicator = SaltIndicator(salt: salt)
            XCTAssertEqual(indicator.backgroundRGB, rgb, salt)
            XCTAssertEqual(indicator.foregroundRGB, 0xFFFFFF - rgb, salt)
            XCTAssertEqual(indicator.iconIndex, index, salt)
            XCTAssertEqual(indicator.iconCodePoint, codePoint, salt)
        }
    }
}
