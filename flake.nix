{
  description = "Kotlin + Android SDK + Rust for the NDK, for Gradle projects";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  # nixpkgs ships one rustc for this machine; the crate also has to compile for the phone.
  inputs.rust-overlay.url = "github:oxalica/rust-overlay";
  inputs.rust-overlay.inputs.nixpkgs.follows = "nixpkgs";

  outputs = { nixpkgs, rust-overlay, ... }:
    let
      system = "x86_64-linux";

      # The Android SDK is unfree and its licence must be accepted explicitly,
      # so this imports nixpkgs with a config instead of using legacyPackages.
      pkgs = import nixpkgs {
        inherit system;
        overlays = [ rust-overlay.overlays.default ];
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };

      buildToolsVersion = "37.0.0";

      android = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "37.0" ];
        buildToolsVersions = [ buildToolsVersion ];
        includeNDK = true;
        ndkVersions = [ "27.2.12479018" ];
      };

      sdk = "${android.androidsdk}/libexec/android-sdk";

      # Must match the ABIs the cargoNdk task in app/build.gradle.kts asks for.
      rust = pkgs.rust-bin.stable.latest.default.override {
        targets = [
          "aarch64-linux-android"
          "armv7-linux-androideabi"
          "x86_64-linux-android"
        ];
      };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        # kotlin-language-server is here because neovim has mason disabled.
        packages = with pkgs; [
          jdk21
          gradle
          kotlin
          kotlin-language-server
          ktlint # the LazyVim kotlin extra wants it via mason, which is off
          android.androidsdk
          android-tools # adb, to install on the phone
          rust
          cargo-ndk
        ];

        ANDROID_HOME = sdk;
        ANDROID_SDK_ROOT = sdk;
        ANDROID_NDK_ROOT = "${sdk}/ndk-bundle";
        JAVA_HOME = "${pkgs.jdk21}";

        # Gradle otherwise fetches a prebuilt aapt2 from Maven that will not run.
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdk}/build-tools/${buildToolsVersion}/aapt2";

        # apksigner and zipalign ship in build-tools, which the SDK package does not put on PATH.
        shellHook = ''
          export PATH="${sdk}/build-tools/${buildToolsVersion}:$PATH"
        '';
      };
    };
}
