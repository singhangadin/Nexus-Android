import com.android.build.api.dsl.LibraryExtension
import ext.configureKotlinAndroid
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class NexusAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    minSdk    = libs.findVersion("minSdk").get().toString().toInt()
                    consumerProguardFiles("consumer-rules.pro")
                }

                buildFeatures {
                    buildConfig = false
                }
            }
        }
    }
}