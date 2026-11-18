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
Requirements: a Mac with Xcode 16+, [XcodeGen](https://github.com/yonaskolb/XcodeGen), and an Apple Developer Program membership. A free Apple ID may not be able to sign the NFC and AutoFill entitlements, and its builds expire after 7 days. No Mac? See [TestFlight](#testflight-no-mac-needed) below.

```sh
brew install xcodegen
cd ios
xcodegen            # generates Ykpass.xcodeproj from project.yml
open Ykpass.xcodeproj
```

Then in Xcode:
1. Select your team under *Signing & Capabilities* for both the `Ykpass` and `YkpassAutoFill` targets. If the bundle ID `org.liran.noam.ykpass` is taken, change `YKPASS_BUNDLE_ID` in `project.yml` and run `xcodegen` again. The extension's ID follows it automatically.
2. Run on a real device. The simulator has no NFC.
3. To use AutoFill, open *Settings → General → AutoFill & Passwords* and turn on **ykpass**.

## TestFlight (no Mac needed)
The **iOS TestFlight** GitHub Actions workflow (`.github/workflows/testflight.yml`) builds, signs and uploads the app to TestFlight. Signing uses Apple's cloud-managed certificates through an App Store Connect API key, so there's no certificate or provisioning profile to export.

### One-time setup
1. **Join the [Apple Developer Program](https://developer.apple.com/programs/)** ($99/year).
2. **Register the bundle IDs** at [developer.apple.com → Identifiers](https://developer.apple.com/account/resources/identifiers/list) (*+ → App IDs → App*):
   - `org.liran.noam.ykpass` (or your own ID): enable **NFC Tag Reading** and **AutoFill Credential Provider**.
   - `org.liran.noam.ykpass.autofill`: enable the same two capabilities.
3. **Create the app** in [App Store Connect](https://appstoreconnect.apple.com/apps) (*+ → New App*): platform iOS, any name (it must be unique on the App Store, e.g. "ykpass-<yourname>"), bundle ID from step 2, any SKU.
4. **Create an API key** in App Store Connect under *Users and Access → Integrations → App Store Connect API → Team Keys*.
   - Give it the **Admin** role. Cloud signing needs it to create the distribution certificate.
   - Download the `.p8` file (you can only download it once). Note the *Key ID* and the *Issuer ID*.
5. **Add repository secrets** under GitHub → *Settings → Secrets and variables → Actions → Secrets*:

   | Secret | Value |
   | --- | --- |
   | `APPLE_TEAM_ID` | Your 10-character Team ID ([Membership details](https://developer.apple.com/account#MembershipDetailsCard)) |
   | `APP_STORE_CONNECT_KEY_ID` | The API key's Key ID |
   | `APP_STORE_CONNECT_ISSUER_ID` | The Issuer ID |
   | `APP_STORE_CONNECT_KEY` | The full contents of the `.p8` file, including the `BEGIN`/`END` lines |

   If you used a different bundle ID, also add it under the **Variables** tab as `IOS_BUNDLE_ID`.

### Uploading a build
1. On GitHub, go to *Actions → iOS TestFlight → Run workflow*.
2. After about 15 minutes, plus a few minutes of processing at Apple, the build appears in App Store Connect under *TestFlight*.
3. Add yourself as an internal tester under *TestFlight → Internal Testing*. Then install the **TestFlight** app on your iPhone and install ykpass from it.

Each build expires after 90 days, so run the workflow again to refresh it. Build numbers come from the workflow run number, which always increases.

## Layout
| Path | Contents |
| --- | --- |
| `YkpassCore/` | Swift package with the platform-independent logic: encodings, password flavors, salt indicator, domain → password ID. It has unit tests checked against the other clients (`swift test`; also runs on Linux). |
| `Shared/` | SwiftUI form, YubiKey service and settings. Used by both the app and the extension. |
| `Ykpass/` | App entry point and assets. |
| `YkpassAutoFill/` | AutoFill credential provider extension. |
| `project.yml` | XcodeGen spec (Info.plist keys, entitlements, the YubiKit dependency). |
