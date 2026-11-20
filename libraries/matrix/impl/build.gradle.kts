import extension.setupDependencyInjection
import extension.testCommonDependencies
import org.gradle.kotlin.dsl.resolver.SourceDistributionResolver.Companion.artifactType

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.impl"
}

setupDependencyInjection()

// `-PelementCallLocalVersion` points this at a locally published build of element-call-android.
// Goes with the mavenLocal block in settings.gradle.kts. Only the BOM carries a version; every other artifact follows it.
val elementCallVersion: String = providers.gradleProperty("elementCallLocalVersion")
    .getOrElse(libs.versions.element.call.get())

dependencies {
<<<<<<< HEAD
    if (usesLocalRustSdk) {
        println(
            "\nNote: Using local binary of the Rust SDK." +
                "\n      element-call-matrix is substituted onto it as well, so the native call runs" +
                "\n      against this SDK rather than the one it was published against.\n"
        )
=======
    releaseImplementation(libs.matrix.sdk)
    if (file("${rootDir.path}/libraries/rustsdk/matrix-rust-sdk.aar").exists()) {
        println("\nNote: Using local binary of the Rust SDK.\n")
>>>>>>> main-element
        debugImplementation(projects.libraries.rustsdk)
        releaseImplementation(projects.libraries.rustsdk)
    } else {
        debugImplementation(libs.matrix.sdk)
        releaseImplementation(libs.matrix.sdk)
    }
    // Disabled until the platform verifier is ready for production. See CertificateVerifier.kt.
//    implementation(variantOf(libs.rustls.platform.verifier) { artifactType("aar") })

    // The native call's MatrixBackend, over the SDK client. Only this module may hand it a raw SDK client.
    // element-call carries the RTC core, where MatrixBackend lives, as an api dependency.
    implementation(platform("io.element.android:element-call-bom:$elementCallVersion"))
    implementation(libs.element.call.impl)
    implementation(projects.features.callnative.api)

    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.networkmonitor.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.network)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.workmanager.api)
    implementation(projects.libraries.tchaputils)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    api(projects.libraries.matrix.api)
    implementation(projects.libraries.core)
    implementation(variantOf(libs.jna) { artifactType("aar") })
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.serialization.json)
    implementation(libs.androidx.lifecycle.process)

    testCommonDependencies(libs)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.previewutils)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.workmanager.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
}
