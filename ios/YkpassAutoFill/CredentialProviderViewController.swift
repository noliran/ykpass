import AuthenticationServices
import SwiftUI
import YkpassCore

/// AutoFill credential provider: the iOS counterpart of the Android autofill service.
/// The password ID is derived from the website's domain the same way as on Android.
///
/// Note: iOS doesn't let app extensions talk to Lightning accessories, so in AutoFill only
/// NFC YubiKeys can be used. Use the main app (and copy) with a Lightning YubiKey.
final class CredentialProviderViewController: ASCredentialProviderViewController {
    private var hostingController: UIHostingController<GeneratorView>?

    private func show(passwordID: String, completion: @escaping (String) -> Void) {
        hostingController?.willMove(toParent: nil)
        hostingController?.view.removeFromSuperview()
        hostingController?.removeFromParent()

        let model = GeneratorModel(passwordID: passwordID)
        let view = GeneratorView(model: model, mode: .autofill(onFill: completion, onCancel: { [weak self] in
            self?.cancel()
        }))
        let hosting = UIHostingController(rootView: view)
        addChild(hosting)
        hosting.view.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(hosting.view)
        NSLayoutConstraint.activate([
            hosting.view.leadingAnchor.constraint(equalTo: self.view.leadingAnchor),
            hosting.view.trailingAnchor.constraint(equalTo: self.view.trailingAnchor),
            hosting.view.topAnchor.constraint(equalTo: self.view.topAnchor),
            hosting.view.bottomAnchor.constraint(equalTo: self.view.bottomAnchor),
        ])
        hosting.didMove(toParent: self)
        hostingController = hosting
    }

    /// Called when the user picks ykpass from the AutoFill (key icon) menu on a login form.
    override func prepareCredentialList(for serviceIdentifiers: [ASCredentialServiceIdentifier]) {
        let passwordID = serviceIdentifiers.first.map { PasswordID.fromServiceIdentifier($0.identifier) } ?? ""
        show(passwordID: passwordID) { [weak self] password in
            let credential = ASPasswordCredential(user: "", password: password)
            self?.extensionContext.completeRequest(withSelectedCredential: credential, completionHandler: nil)
        }
    }

    /// Called from the "AutoFill → Passwords" text-insertion menu in any text field (iOS 18+).
    @available(iOS 18.0, *)
    override func prepareInterfaceForUserChoosingTextToInsert() {
        show(passwordID: "") { [weak self] password in
            self?.extensionContext.completeRequest(withTextToInsert: password, completionHandler: nil)
        }
    }

    /// ykpass never stores credentials, so it can't fill without asking for the salt.
    override func provideCredentialWithoutUserInteraction(for credentialIdentity: ASPasswordCredentialIdentity) {
        extensionContext.cancelRequest(withError: NSError(
            domain: ASExtensionErrorDomain,
            code: ASExtensionError.userInteractionRequired.rawValue
        ))
    }

    private func cancel() {
        extensionContext.cancelRequest(withError: NSError(
            domain: ASExtensionErrorDomain,
            code: ASExtensionError.userCanceled.rawValue
        ))
    }
}
