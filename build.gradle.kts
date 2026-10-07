plugins {
    id("com.android.application") version "9.4.0" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}
