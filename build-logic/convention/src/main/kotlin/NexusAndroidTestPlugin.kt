import com.android.build.api.dsl.TestExtension
import ext.configureKotlinAndroid
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class NexusAndroidTestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.test")

            extensions.configure<TestExtension> {
                configureKotlinAndroid(this)

                targetProjectPath = ":app"
                experimentalProperties["android.experimental.self-instrumenting"] = true

                defaultConfig {
                    minSdk    = libs.findVersion("minSdk").get().toString().toInt()
                    targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("benchmark-macro-junit4").get())
            }
        }
    }
}
