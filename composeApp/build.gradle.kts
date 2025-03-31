import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Properties

private val applicationId = "com.alpenraum.shimstack"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildKonfig)
    id("kotlin-parcelize") // needed only for non-primitive classes
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-P",
                        "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=cl.emilym.kmp.parcelable.Parcelize"
                    )
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.material)
            implementation(libs.play.services.location)
            implementation(libs.maps.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.koin.compose.viewmodel.nav)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            api(libs.koin.annotations)

            implementation(libs.touchlab.kermit)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material3.window.size)

            implementation(libs.kotlinx.collections.immutable)

            implementation(libs.eygraber.compose.placeholder)

            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.datastore.preferences)
            implementation(libs.datastore)
            implementation(libs.kotlinx.datetime)
            implementation(libs.parcelable)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.json)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.auth)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

room {
    schemaDirectory("$projectDir/db-schemas")
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosSimulatorArm64",
        "kspIosX64",
        "kspIosArm64"
    ).forEach {
        add(it, libs.koin.annotations.ksp)
        add(it, libs.androidx.room.compiler)
    }
}

android {
    namespace = applicationId
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = this@Build_gradle.applicationId
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        val properties = getProperties()
        val apikey = properties.getProperty("maps_android_sdk_key")
        manifestPlaceholders["maps_api_key"] = apikey
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            manifestPlaceholders["app_name"] = "@string/app_name"
        }
        getByName("debug") {
            applicationIdSuffix = ".dev"
            manifestPlaceholders["app_name"] = "@string/app_name_debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.annotations.ksp)
    add("kspAndroid", libs.koin.annotations.ksp)
    add("kspIosX64", libs.koin.annotations.ksp)
    add("kspIosArm64", libs.koin.annotations.ksp)
    add("kspIosSimulatorArm64", libs.koin.annotations.ksp)
}

fun getProperties(): Properties {
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())
    return properties
}

// KSP Metadata Trigger
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_CONFIG_CHECK", "false")
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

buildkonfig {
    this.packageName = applicationId

    val properties = getProperties()
    val shimstackDevApiKey = properties.getProperty("shimstack_dev_api_key") // TODO: SECURE BE USING GOOGLE PLAY INTEGRITY API

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "baseUrl", "https://localhost:8080") // check exact url through tailscale
        buildConfigField(FieldSpec.Type.STRING, "authSecret", "TODO")
    }
    // changeable in gradle.properties or via -Pbuildkonfig.flavor=release
    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.STRING, "baseUrl", "https://localhost:8080")
        buildConfigField(FieldSpec.Type.STRING, "authSecret", shimstackDevApiKey)
    }
}