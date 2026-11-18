// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "YkpassCore",
    platforms: [.iOS(.v16), .macOS(.v12)],
    products: [
        .library(name: "YkpassCore", targets: ["YkpassCore"]),
    ],
    targets: [
        .target(name: "YkpassCore"),
        .testTarget(name: "YkpassCoreTests", dependencies: ["YkpassCore"]),
    ]
)
