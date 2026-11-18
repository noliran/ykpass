import CoreNFC
import Foundation
import YubiKit

enum YubiKeySlot: Int, CaseIterable, Identifiable {
    case one = 1
    case two = 2

    var id: Int { rawValue }

    var ykfSlot: YKFSlot {
        switch self {
        case .one: return .one
        case .two: return .two
        }
    }
}

enum YubiKeyError: LocalizedError {
    case noTransportAvailable
    case busy
    case cancelled
    case emptyResponse
    case failed(String)

    var errorDescription: String? {
        switch self {
        case .noTransportAvailable:
            return "Plug in a Lightning YubiKey, or use a device that can read NFC YubiKeys."
        case .busy:
            return "Already waiting for a YubiKey."
        case .cancelled:
            return "Cancelled."
        case .emptyResponse:
            return "The YubiKey returned an empty response. Is challenge-response configured on this slot?"
        case let .failed(message):
            return message
        }
    }
}

/// Runs HMAC-SHA1 challenge-response on a YubiKey, over Lightning (YubiKey 5Ci) when one is
/// plugged in, or over NFC otherwise. (USB-C keys are only reachable via iOS's smart card
/// interface, which doesn't expose the OTP application, so they must be used over NFC.)
final class YubiKeyService: NSObject, ObservableObject {
    static let shared = YubiKeyService()

    /// True while a Lightning YubiKey is connected.
    @Published private(set) var accessoryConnected = false

    var nfcAvailable: Bool { YubiKitDeviceCapabilities.supportsISO7816NFCTags }

    private struct Pending {
        let challenge: Data
        let slot: YKFSlot
        let continuation: CheckedContinuation<Data, Error>
        var overNFC = false
    }

    private var accessoryConnection: YKFAccessoryConnection?
    private var pending: Pending?

    private override init() {
        super.init()
        YubiKitManager.shared.delegate = self
        // App extensions (AutoFill) can't use Lightning accessories, only NFC.
        let isExtension = Bundle.main.bundlePath.hasSuffix(".appex")
        if !isExtension && YubiKitDeviceCapabilities.supportsMFIAccessoryKey {
            YubiKitManager.shared.startAccessoryConnection()
        }
    }

    /// Sends `challenge` to `slot` and returns the 20-byte HMAC-SHA1 response.
    @MainActor
    func challengeResponse(_ challenge: Data, slot: YubiKeySlot) async throws -> Data {
        guard pending == nil else { throw YubiKeyError.busy }

        return try await withCheckedThrowingContinuation { continuation in
            pending = Pending(challenge: challenge, slot: slot.ykfSlot, continuation: continuation)

            if let accessoryConnection {
                run(on: accessoryConnection)
            } else if nfcAvailable {
                pending?.overNFC = true
                YubiKitManager.shared.startNFCConnection()
            } else {
                finish(.failure(YubiKeyError.noTransportAvailable))
            }
        }
    }

    private func run(on connection: YKFConnectionProtocol) {
        guard let request = pending else { return }

        connection.challengeResponseSession { [weak self] session, error in
            guard let session else {
                self?.finish(.failure(error.map(Self.wrap) ?? YubiKeyError.failed("Could not open a challenge-response session.")))
                return
            }
            session.sendChallenge(request.challenge, slot: request.slot) { response, error in
                if let error {
                    self?.finish(.failure(Self.wrap(error)))
                } else if let response, !response.isEmpty {
                    self?.finish(.success(response))
                } else {
                    self?.finish(.failure(YubiKeyError.emptyResponse))
                }
            }
        }
    }

    private func finish(_ result: Result<Data, Error>) {
        DispatchQueue.main.async { [self] in
            guard let request = pending else { return }
            pending = nil

            if request.overNFC {
                switch result {
                case .success:
                    YubiKitManager.shared.stopNFCConnection(withMessage: "Password generated")
                case let .failure(error):
                    if case YubiKeyError.cancelled = error {
                        YubiKitManager.shared.stopNFCConnection()
                    } else {
                        YubiKitManager.shared.stopNFCConnection(withErrorMessage: error.localizedDescription)
                    }
                }
            }
            request.continuation.resume(with: result)
        }
    }

    private static func wrap(_ error: Error) -> Error {
        if let nfcError = error as? NFCReaderError,
           nfcError.code == .readerSessionInvalidationErrorUserCanceled {
            return YubiKeyError.cancelled
        }
        return YubiKeyError.failed(error.localizedDescription)
    }
}

extension YubiKeyService: YKFManagerDelegate {
    func didConnectNFC(_ connection: YKFNFCConnection) {
        DispatchQueue.main.async { [self] in
            guard pending?.overNFC == true else { return }
            run(on: connection)
        }
    }

    func didDisconnectNFC(_ connection: YKFNFCConnection, error: Error?) {
        DispatchQueue.main.async { [self] in
            guard pending?.overNFC == true else { return }
            finish(.failure(error.map(Self.wrap) ?? YubiKeyError.cancelled))
        }
    }

    func didFailConnectingNFC(_ error: Error) {
        DispatchQueue.main.async { [self] in
            guard pending?.overNFC == true else { return }
            finish(.failure(Self.wrap(error)))
        }
    }

    func didConnectAccessory(_ connection: YKFAccessoryConnection) {
        DispatchQueue.main.async { [self] in
            accessoryConnection = connection
            accessoryConnected = true
        }
    }

    func didDisconnectAccessory(_ connection: YKFAccessoryConnection, error: Error?) {
        DispatchQueue.main.async { [self] in
            accessoryConnection = nil
            accessoryConnected = false
            if let request = pending, !request.overNFC {
                finish(.failure(YubiKeyError.failed("The YubiKey was disconnected.")))
            }
        }
    }
}
