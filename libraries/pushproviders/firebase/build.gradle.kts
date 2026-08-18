/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.ResValue
import config.BuildTimeConfig
import extension.setupDependencyInjection
import extension.testCommonDependencies
import org.gradle.kotlin.dsl.withType
import org.sonarqube.gradle.SonarResolverTask

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.pushproviders.firebase"

    buildFeatures {
        resValues = true
        // :tchap: Push config depending of buildConfig
        buildConfig = true
        // :tchap: end
    }

    // :tchap: Push config for Gateway URL & Variants
//    buildTypes {
//        getByName("release") {
//            consumerProguardFiles("consumer-proguard-rules.pro")
//            resValue(
//                type = "string",
//                name = "google_app_id",
//                value = BuildTimeConfig.GOOGLE_APP_ID_RELEASE,
//            )
//        }
//        getByName("debug") {
//            resValue(
//                type = "string",
//                name = "google_app_id",
//                value = BuildTimeConfig.GOOGLE_APP_ID_DEBUG,
//            )
//        }
//        register("nightly") {
//            consumerProguardFiles("consumer-proguard-rules.pro")
//            matchingFallbacks += listOf("release")
//            resValue(
//                type = "string",
//                name = "google_app_id",
//                value = BuildTimeConfig.GOOGLE_APP_ID_NIGHTLY,
//            )
//        }
//    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("consumer-proguard-rules.pro")
        }
        register("nightly") {
            consumerProguardFiles("consumer-proguard-rules.pro")
            matchingFallbacks += listOf("release")
        }
    }

    defaultConfig {
        buildConfigField("String", "pushConfigGatewayURL", "\"\"")
    }
    // :tchap: end
}

// :tchap: Push config for Gateway URL & Variants
androidComponents {
    onVariants { variant ->
        val targetFlavor = variant.productFlavors.find { it.first == "target" }?.second

        var appId = ""
        var gatewayUrl = ""

        when (targetFlavor) {
            "tchap" -> {
                appId = when (variant.buildType) {
                    "release" -> BuildTimeConfig.GOOGLE_APP_ID_PROD
                    "nightly" -> BuildTimeConfig.GOOGLE_APP_ID_PROD_NIGHTLY
                    else -> BuildTimeConfig.GOOGLE_APP_ID_PROD_DEBUG
                }
                gatewayUrl = BuildTimeConfig.PUSH_CONFIG_GATEWAY_URL_PROD
            }
            "tchapPreprod" -> {
                appId = when (variant.buildType) {
                    "release" -> BuildTimeConfig.GOOGLE_APP_ID_PREPROD
                    "nightly" -> BuildTimeConfig.GOOGLE_APP_ID_PREPROD_NIGHTLY
                    else -> BuildTimeConfig.GOOGLE_APP_ID_PREPROD_DEBUG
                }
                gatewayUrl = BuildTimeConfig.PUSH_CONFIG_GATEWAY_URL_PREPROD
            }
            "tchapDev" -> {
                appId = when (variant.buildType) {
                    "release" -> BuildTimeConfig.GOOGLE_APP_ID_DEV
                    "nightly" -> BuildTimeConfig.GOOGLE_APP_ID_DEV_NIGHTLY
                    else -> BuildTimeConfig.GOOGLE_APP_ID_DEV_DEBUG
                }
                gatewayUrl = BuildTimeConfig.PUSH_CONFIG_GATEWAY_URL_DEV
            }
        }

        variant.resValues.put(
            variant.makeResValueKey("string", "google_app_id"),
            ResValue(appId, null)
        )

        variant.buildConfigFields?.put(
            "pushConfigGatewayURL",
            BuildConfigField("String", "\"$gatewayUrl\"", null)
        )
    }
}
// :tchap: end

// Configure the SonarQube plugin to wait for the resource generation tasks to complete before running the analysis.
tasks.withType<SonarResolverTask>().configureEach {
    dependsOn("generateDebugResValues", "generateDebugAndroidTestResValues")
}

setupDependencyInjection()

dependencies {
    implementation(libs.androidx.corektx)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.services.toolbox.api)

    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.pushproviders.api)

    api(platform(libs.google.firebase.bom))
    api("com.google.firebase:firebase-messaging") {
        exclude(group = "com.google.firebase", module = "firebase-core")
        exclude(group = "com.google.firebase", module = "firebase-analytics")
        exclude(group = "com.google.firebase", module = "firebase-measurement-connector")
    }

    testCommonDependencies(libs)
    testImplementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.troubleshoot.test)
    testImplementation(projects.services.toolbox.test)
}
