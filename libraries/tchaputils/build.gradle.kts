plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "fr.gouv.tchap.libraries.tchaputils"
}

dependencies {
    if (file("${rootDir.path}/libraries/rustsdk/matrix-rust-sdk.aar").exists()) {
        println("\nNote: Using local binary of the Rust SDK.\n")
        debugImplementation(projects.libraries.rustsdk)
        releaseImplementation(projects.libraries.rustsdk)
    } else {
        debugImplementation(libs.matrix.sdk)
        releaseImplementation(libs.matrix.sdk)
    }

    testImplementation(libs.test.junit)
}
