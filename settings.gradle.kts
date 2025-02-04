rootProject.name = "Appwrite"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri(
                "https://anwarpro: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@maven.pkg.github.com/anwarpro/sdk-for-kmm"
            )
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":composeApp", ":library")

rootProject.children.forEach {
    it.name = if (it.name == "library") "sdk-for-kmm" else it.name
}