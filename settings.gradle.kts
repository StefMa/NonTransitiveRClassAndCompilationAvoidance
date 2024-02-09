pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NonTransitiveRClassAndCompilationAvoidance"
include(":app")
include(":lib1")
include(":lib2")
include(":lib3")
include(":lib4")
include(":libstrings")
 