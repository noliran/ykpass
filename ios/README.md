# ykpass for iOS

A native SwiftUI port of ykpassDroid. It uses Yubico's [YubiKit](https://github.com/Yubico/yubikit-ios) to run HMAC-SHA1 challenge-response on your YubiKey and produces **the same passwords** as the Android and desktop clients.

## Features
- **Salt / Password ID / Method** form, the same as on Android. The salt indicator (color + icon) is also identical, so a salt that looks right on one device looks the same on the others.
- A trailing `!`, `@`, `#` or `$` on the password ID selects the method, like the desktop scripts.
- **YubiKey over NFC** (YubiKey 5 NFC, 5C NFC) or **Lightning** (YubiKey 5Ci).
- **Copy** to the clipboard. The copy stays on this device (no Universal Clipboard) and clears itself after a set time (1 minute by default).
- **AutoFill provider**, the iOS counterpart of the Android autofill service. On a login form, tap the key icon above the keyboard and pick *ykpass*. The password ID is filled in from the site's domain using the same rules as Android (`accounts.google.com` → `google`). On iOS 18+ you can also use *AutoFill → Passwords* from the edit menu of any text field.
- Settings: YubiKey slot (default **slot 2**), numeric or full keyboard for the salt, and the clipboard timeout.

### Limitations
- **USB-C YubiKeys** connect to iOS only as smart cards, and that interface can't reach the OTP/challenge-response application, so tap them over NFC instead (e.g. a YubiKey 5C NFC).
- **AutoFill only works with NFC keys**, because app extensions can't use Lightning accessories. With a 5Ci, generate the password in the app and copy it.

## Building
Requirements: a Mac with Xcode 16+, [XcodeGen](https://github.com/yonaskolb/XcodeGen), and an Apple developer account. A free account works for running on your own device. NFC tag reading and AutoFill entitlements need a team.

```sh
brew install xcodegen
cd ios
xcodegen            # generates Ykpass.xcodeproj from project.yml
open Ykpass.xcodeproj
```

Then in Xcode:
1. Select your team under *Signing & Capabilities* for both the `Ykpass` and `YkpassAutoFill` targets. If the bundle ID `org.liran.noam.ykpass` is taken, change it, and keep the extension's ID as `<app id>.autofill`.
2. Run on a real device. The simulator has no NFC.
3. To use AutoFill, open *Settings → General → AutoFill & Passwords* and turn on **ykpass**.

## Layout
| Path | Contents |
| --- | --- |
| `YkpassCore/` | Swift package with the platform-independent logic: encodings, password flavors, salt indicator, domain → password ID. It has unit tests checked against the other clients (`swift test`; also runs on Linux). |
| `Shared/` | SwiftUI form, YubiKey service and settings. Used by both the app and the extension. |
| `Ykpass/` | App entry point and assets. |
| `YkpassAutoFill/` | AutoFill credential provider extension. |
| `project.yml` | XcodeGen spec (Info.plist keys, entitlements, the YubiKit dependency). |
