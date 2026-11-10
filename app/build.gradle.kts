import javax.inject.Inject

plugins {
    id("com.android.application")
    kotlin("plugin.compose")
    id("org.jmailen.kotlinter")
}

android {
    namespace = "com.tonekyilay.romaji"
    compileSdk { version = release(37) }
    // The Nix SDK is read-only, so this must be the build-tools version flake.nix installs.
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.tonekyilay.romaji"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "3.0.0"
        // Only the ABIs cargoNdk builds, or a device picks one with no libromaji.so and crashes.
        ndk.abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
    }

    // The release key's path and password live in ~/.gradle/gradle.properties, never in the repo.
    val releaseStore = providers.gradleProperty("ROMAJI_RELEASE_STORE_FILE").orNull
    signingConfigs {
        if (releaseStore != null) {
            create("release") {
                storeFile = file(releaseStore)
                storePassword = providers.gradleProperty("ROMAJI_RELEASE_STORE_PASSWORD").get()
                keyAlias = "release"
                // PKCS12, keytool's default, allows only one password for the store and the key.
                keyPassword = storePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Without the key the release stays unsigned, never debug-signed.
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures.compose = true

    // Cargo already strips; this skips AGP looking for an NDK of its own to strip with.
    packaging.jniLibs.keepDebugSymbols += "**/libromaji.so"
}

/** Builds the Rust crate for each phone ABI into the layout AGP packages as jniLibs. */
abstract class CargoNdk : DefaultTask() {
    @get:Internal
    abstract val crateDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun build() {
        execOps.exec {
            workingDir = crateDir.get().asFile
            // arm64 is every modern phone, armv7 the old 32-bit ones, x86_64 the emulator.
            commandLine(
                "cargo", "ndk",
                "-t", "arm64-v8a", "-t", "armeabi-v7a", "-t", "x86_64",
                "-P", "26",
                "-o", outputDir.get().asFile.path,
                "build", "--release",
            )
        }
    }
}

val cargoNdk = tasks.register<CargoNdk>("cargoNdk") {
    val crate = rootProject.layout.projectDirectory.dir("rust/romaji")
    crateDir = crate
    sources.from(crate.asFileTree.matching { include("src/**", "Cargo.toml", "Cargo.lock", ".cargo/**") })
}

androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(cargoNdk, CargoNdk::outputDir)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    testImplementation("junit:junit:4.13.2")
}
