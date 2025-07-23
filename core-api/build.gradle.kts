import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.homecare.vod.api"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.gson.convert)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.google.gson)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    configure(
        AndroidSingleVariantLibrary(
            // the published variant
            variant = "release",
            // whether to publish a sources jar
            sourcesJar = true,
            // whether to publish a javadoc jar
            publishJavadocJar = true,
        )
    )

    coordinates("io.github.zhangwenxue", "homecarevod-core-api", "0.0.2")

    pom {
        name.set("HomeCareVod Core,.Lib")
        description.set("HomeCareVod Core Api")
        inceptionYear.set("2025")
        url.set("https://github.com/zhangwenxue/HomeCareVod/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("zwx")
                name.set("zhangwenxue")
                url.set("https://github.com/zhangwenxue/")
            }
        }
        scm {
            url.set("https://github.com/zhangwenxue/HomeCareVod/")
            connection.set("scm:git:git://github.com/zhangwenxue/HomeCareVod.git")
            developerConnection.set("scm:git:ssh://git@github.com/zhangwenxue/HomeCareVod.git")
        }
    }
    signAllPublications()
}