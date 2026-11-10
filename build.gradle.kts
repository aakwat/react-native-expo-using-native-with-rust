plugins {
    id("com.android.application") version "9.5.1" apply false
    // Never applied: it only pins the Kotlin compiler that AGP's built-in Kotlin picks up.
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.compose") version "2.4.21" apply false
    id("org.jmailen.kotlinter") version "5.7.0" apply false
}
