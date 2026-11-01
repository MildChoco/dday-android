buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // AGP 9는 Kotlin을 내장하고 있다. 내장 Kotlin을 Compose 컴파일러 플러그인과 같은 버전으로 올린다.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
