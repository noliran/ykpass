import CoreGraphics
import CoreText
import SwiftUI
import YkpassCore

/// Shows the salt's color + icon fingerprint (same as the Android app), so a mistyped salt is
/// noticeable before a password is generated.
struct SaltIndicatorView: View {
    let salt: String

    var body: some View {
        let indicator = SaltIndicator(salt: salt)
        Text(indicator.icon)
            .font(FontAwesome.font(size: 17))
            .foregroundColor(Color(rgb: indicator.foregroundRGB))
            .frame(width: 32, height: 32)
            .background(
                RoundedRectangle(cornerRadius: 5)
                    .fill(Color(rgb: indicator.backgroundRGB))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 5)
                    .stroke(Color.black, lineWidth: 1.5)
            )
            .accessibilityHidden(true)
    }
}

extension Color {
    init(rgb: UInt32) {
        self.init(
            red: Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8) & 0xFF) / 255,
            blue: Double(rgb & 0xFF) / 255
        )
    }
}

enum FontAwesome {
    /// Registers the bundled Font Awesome font (shared with the Android app) and returns its name.
    private static let postScriptName: String? = {
        guard let url = Bundle.main.url(forResource: "fa-solid-900", withExtension: "ttf"),
              let provider = CGDataProvider(url: url as CFURL),
              let font = CGFont(provider) else {
            return nil
        }
        CTFontManagerRegisterGraphicsFont(font, nil) // Fails harmlessly if already registered.
        return font.postScriptName as String?
    }()

    static func font(size: CGFloat) -> Font {
        guard let name = postScriptName else { return .system(size: size) }
        return .custom(name, fixedSize: size)
    }
}
