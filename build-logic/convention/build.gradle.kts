plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("nexusAndroidApplication") {
            id = "nexus.android.application"
            implementationClass = "NexusAndroidApplicationPlugin"
        }
        register("nexusAndroidLibrary") {
            id = "nexus.android.library"
            implementationClass = "NexusAndroidLibraryPlugin"
        }
        register("nexusAndroidFeature") {
            id = "nexus.android.feature"
            implementationClass = "NexusAndroidFeaturePlugin"
        }
        register("nexusAndroidCompose") {
            id = "nexus.android.compose"
            implementationClass = "NexusAndroidComposePlugin"
        }
        register("nexusHilt") {
            id = "nexus.hilt"
            implementationClass = "NexusHiltPlugin"
        }
        register("nexusAndroidTest") {
            id = "nexus.android.test"
            implementationClass = "NexusAndroidTestPlugin"
        }
    }
}