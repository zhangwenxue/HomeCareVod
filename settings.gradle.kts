pluginManagement {
    repositories {

        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/google")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven(url = "https://mirrors.163.com/maven/repository/maven-public/")
//        maven(url = "https://repo.huaweicloud.com/repository/maven/")
        mavenCentral()

        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        google()
    }
}

rootProject.name = "HomeCareVod"
include(":app")
include(":feature-vod")
include(":core-api")
include(":tuicallkit-kt")
