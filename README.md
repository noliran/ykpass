# ykpass
ykpass is a toolset that allows you to use the challenge-response capabilities of your YubiKey to generate consistent passwords, based on a secret pre-programmed into the YubiKey and on a "salt" provided by the program.

ykpass is *not* a password manager, in the sense that you *don't* need to synchronize your passwords across devices or back them up somewhere. You only need to backup/remember one password, and the salt/"pin code" which you provide at runtime.

# How to use
1. Program one YubiKey (or preferably, more) with your secret
2. Use one of the apps below to generate a password, based on the site name / URL.

# Setting up your YubiKey
Using the [YubiKey Personalization Tool](https://www.yubico.com/products/services-software/download/yubikey-personalization-tools/), set up your YubiKey with these parameters:
* HMAC-SHA1 Challenge-Response
* Variable input
* Require user input (button press)
* Choose a good secret :)

# Deployment
## Mac
1. Clone, build and install [ascii85](https://github.com/roukaour/ascii85)
1. Make sure you have the CLI version of [YubiKey Personalization Tool](https://www.yubico.com/products/services-software/download/yubikey-personalization-tools/) installed
1. Clone this repo
1. Open `Keychain Access` and add a new Keychain item called `ykpass`. This will contain the salt/"pin code" for your passwords.
1. Use either `ykpass.osx.cli` or `ykpass.osx.ui`:
* `ykpass.osx.cli` will write the generated password into stdout.
* `ykpass.osx.ui` will type the passwords to the foreground application. It is best to integrate it with some kind of hotkey using Alfred or BetterTouchTool.

## Tweaks
You should consider implementing a *DEFAULT_CHALLENGE* script.
Set the environment variable DEFAULT_CHALLENGE to point to a script, whose output will be used as the default site name.
I recommend using Chrome's foreground domain name as the default value; The following works best for me in Mac:
`osascript -e 'tell application "Google Chrome" to set theURL to URL of active tab of front window'`

## Android
1. Build the ykpassDroid app and install (hopefully I'll find time to deploy it to the Play Store).
1. Install [ykDroid app](https://play.google.com/store/apps/details?id=net.pp3345.ykdroid) from Play Store (or build from [source](https://github.com/pp3345/ykDroid))

# Password generation flavors
There are several password flavors, determined by the suffix of the requested site name / URL:
* `!      ` - 16 characters of Base64 (`A-Z a-z 0-9 +/`)
* `@      ` - 12 characters of Base32 (`A–Z 2–7`).
* `#      ` - 8 characters of Base32 (`A–Z 2–7`).

The default if no suffix is present is 16 characters of Ascii85 (`A-Z a-u 0-9 !"#$%&\'()*+,-./:;<=>?@[\\]^_`).


# Known issues
## Concept
1. Password expiration - there's no easy/built-in way to manage password expiration. You can "implement" it on your own by adding some kind of suffix (like 1, 2, etc). I get around this by having my DEFAULT_CHALLENGE script to map the current site name to the "updated" site name, like slashdot -> slashdot2. Be sure to keep that backed up ;)
1. Inclusion of all character classes - passwords are not guaranteed to have all character classes (both lower and upper letters, special characters).

## Mac
1. In order to type the passwords in fields that are protected by the Secure Keyboard Entry feature, the `ykchalresp` binary must be setuid to root. I believe the best way to avoid this is to actually use a browser extension.

# Disclaimer
1. This project relies on cryptographic concepts but has not been audited/validated by a cryptographer.
1. This project is not affiliated with Yubico or YubiKey, which are the registered trademarks of Yubico.
