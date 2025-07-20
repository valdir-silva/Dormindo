plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
}

android {
    namespace = "net.alunando.dormindo"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.alunando.dormindo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Media Control
    implementation(libs.androidx.media)
    implementation(libs.androidx.media3.session)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    testImplementation(libs.mockk)
    implementation("androidx.compose.material:material-icons-extended")
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// Configuração do JaCoCo para cobertura de testes unitários
jacoco {
    toolVersion = "0.8.11"
}

// Task customizada para gerar o relatório de cobertura
// (apenas para testes unitários)
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        // Excluir arquivos gerados e de teste
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = fileTree(
        mapOf(
            "dir" to "$buildDir/tmp/kotlin-classes/debug",
            "excludes" to fileFilter
        )
    )
    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files("$buildDir/jacoco/testDebugUnitTest.exec"))
}