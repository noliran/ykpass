import SwiftUI
import YkpassCore

/// The password generator form, shared by the app and the AutoFill extension.
struct GeneratorView: View {
    enum Mode {
        /// The main app: shows the result with copy/clear actions.
        case standalone
        /// The AutoFill extension: hands the password back as soon as it is generated.
        case autofill(onFill: (String) -> Void, onCancel: () -> Void)
    }

    private enum Field { case salt, passwordID }

    @ObservedObject var model: GeneratorModel
    let mode: Mode

    @AppStorage(Settings.numericSaltKey) private var numericSalt = true
    @FocusState private var focus: Field?
    @State private var showSettings = false
    @State private var copied = false

    private var isAutofill: Bool {
        if case .autofill = mode { return true }
        return false
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Salt") {
                    HStack(spacing: 12) {
                        SaltIndicatorView(salt: model.salt)
                        SecureField("Salt", text: $model.salt)
                            .keyboardType(numericSalt ? .numberPad : .asciiCapable)
                            .focused($focus, equals: .salt)
                            .submitLabel(.next)
                            .onSubmit { focus = .passwordID }
                    }
                }

                Section {
                    TextField("e.g. google", text: $model.passwordID)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .keyboardType(.asciiCapable)
                        .focused($focus, equals: .passwordID)
                        .submitLabel(.go)
                        .onSubmit(generate)
                } header: {
                    Text("Password ID")
                } footer: {
                    Text("A trailing ! @ # or $ selects the method, as on the desktop.")
                }

                Section("Method") {
                    Picker("Method", selection: $model.flavor) {
                        ForEach(PasswordFlavor.allCases) { flavor in
                            HStack {
                                Text(flavor.label).frame(minWidth: 56, alignment: .leading)
                                Text(flavor.detail).foregroundColor(.secondary)
                            }
                            .tag(flavor)
                        }
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }

                Section {
                    Button(action: generate) {
                        HStack {
                            Spacer()
                            if model.isWaiting {
                                ProgressView().padding(.trailing, 4)
                            }
                            Text(isAutofill ? "Generate & Fill" : "Generate").bold()
                            Spacer()
                        }
                    }
                    .disabled(!model.canGenerate)
                } footer: {
                    statusText
                }

                if case .standalone = mode, !model.result.isEmpty {
                    resultSection
                }
            }
            .navigationTitle("ykpass")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
            .toolbar {
                ToolbarItemGroup(placement: .keyboard) {
                    Spacer()
                    if focus == .salt {
                        Button("Next") { focus = .passwordID }
                    } else {
                        Button("Generate", action: generate).disabled(!model.canGenerate)
                    }
                }
            }
            .sheet(isPresented: $showSettings) { SettingsView() }
            .onAppear { focus = .salt }
        }
    }

    @ViewBuilder
    private var statusText: some View {
        switch model.status {
        case .idle:
            EmptyView()
        case let .waiting(message):
            Text(message)
        case let .failed(message):
            Text(message).foregroundColor(.red)
        }
    }

    private var resultSection: some View {
        Section("Result") {
            HStack {
                Text(model.result)
                    .font(.system(.title3, design: .monospaced))
                    .textSelection(.enabled)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                Spacer()
                Button {
                    model.copyResult()
                    copied = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { copied = false }
                } label: {
                    Label(copied ? "Copied" : "Copy", systemImage: copied ? "checkmark" : "doc.on.doc")
                }
                .buttonStyle(.borderless)
            }
            Button("Clear", role: .destructive) {
                model.clear()
                focus = .passwordID
            }
        }
    }

    @ToolbarContentBuilder
    private var toolbar: some ToolbarContent {
        if case let .autofill(_, onCancel) = mode {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel", action: onCancel)
            }
        }
        ToolbarItem(placement: .primaryAction) {
            Button { showSettings = true } label: {
                Image(systemName: "gearshape")
            }
            .accessibilityLabel("Settings")
        }
    }

    private func generate() {
        guard model.canGenerate else { return }
        focus = nil
        Task {
            guard let password = await model.generate() else { return }
            if case let .autofill(onFill, _) = mode {
                onFill(password)
            }
        }
    }
}
