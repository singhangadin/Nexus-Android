import com.android.build.api.dsl.ApplicationExtension
import ext.configureKotlinAndroid
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

class NexusAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9.x auto-applies org.jetbrains.kotlin.android — do not apply it manually
            with(pluginManager) {
                apply("com.android.application")
                apply("nexus.android.compose")
                apply("nexus.hilt")
            }

            val localProps = Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) load(file.inputStream())
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    minSdk      = libs.findVersion("minSdk").get().toString().toInt()
                    targetSdk   = libs.findVersion("targetSdk").get().toString().toInt()
                    versionCode = libs.findVersion("versionCode").get().toString().toInt()
                    versionName = libs.findVersion("versionName").get().toString()
                }

                signingConfigs {
                    create("release") {
                        storeFile     = localProps["KEYSTORE_PATH"]?.let { file(it) }
                        storePassword = localProps["KEYSTORE_PASSWORD"] as String?
                        keyAlias      = localProps["KEY_ALIAS"] as String?
                        keyPassword   = localProps["KEY_PASSWORD"] as String?
                    }
                }

                buildTypes {
                    debug {
                        applicationIdSuffix = ".debug"
                        isDebuggable        = true
                        buildConfigField("String", "API_BASE_URL",
                            "\"${localProps["DEBUG_API_URL"] ?: "http://10.0.2.2:3000/api/v1"}\"")
                        buildConfigField("String", "WS_BASE_URL",
                            "\"${localProps["DEBUG_WS_URL"] ?: "ws://10.0.2.2:3000/ws"}\"")
                    }

                    create("staging") {
                        applicationIdSuffix = ".staging"
                        isDebuggable        = false
                        isMinifyEnabled     = true
                        isShrinkResources   = true
                        signingConfig       = signingConfigs.getByName("release")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                        buildConfigField("String", "API_BASE_URL",
                            "\"${localProps["STAGING_API_URL"] ?: ""}\"")
                        buildConfigField("String", "WS_BASE_URL",
                            "\"${localProps["STAGING_WS_URL"] ?: ""}\"")
                    }

                    release {
                        isDebuggable      = false
                        isMinifyEnabled   = true
                        isShrinkResources = true
                        signingConfig     = signingConfigs.getByName("release")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                        buildConfigField("String", "API_BASE_URL",
                            "\"${localProps["PROD_API_URL"] ?: ""}\"")
                        buildConfigField("String", "WS_BASE_URL",
                            "\"${localProps["PROD_WS_URL"] ?: ""}\"")
                        manifestPlaceholders["enableFlagSecure"] = "true"
                    }
                }

                buildFeatures {
                    buildConfig = true   // compose = true is handled by nexus.android.compose
                }
            }
        }
    }
}