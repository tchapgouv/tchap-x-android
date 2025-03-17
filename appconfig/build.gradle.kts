/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.appconfig"
    
    flavorDimensions += "environment"
    productFlavors {
        create("prod") {
            dimension = "environment"
        }
        create("stg") {
            dimension = "environment"
        }
        create("dev") {
            dimension = "environment"
        }
    }

    defaultConfig {
        missingDimensionStrategy("environment", "prod")
    }
}

dependencies {
    implementation(libs.androidx.annotationjvm)
    implementation(projects.libraries.matrix.api)
}
