plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Numéro de compilation GitHub : chaque nouvelle version a un numéro plus grand,
// ce qu'Android exige pour installer une mise à jour.
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

// Clé de signature : écrite dans app/carhome-release.p12 par GitHub à partir du secret
// CARHOME_KEYSTORE_BASE64 ; son mot de passe vient du secret CARHOME_KEYSTORE_PASSWORD.
// La même clé signe toutes les versions, donc les mises à jour s'installent par-dessus.
val keystorePassword: String? = System.getenv("CARHOME_KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
    ?.takeIf { file("carhome-release.p12").exists() }

android {
    namespace = "com.brahim.carhome"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.brahim.carhome"
        minSdk = 24
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.$buildNumber"
    }

    signingConfigs {
        if (keystorePassword != null) {
            create("release") {
                storeFile = file("carhome-release.p12")
                storeType = "pkcs12"
                storePassword = keystorePassword
                keyAlias = "carhome"
                keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        release {
            // R8 : retire le code inutilisé et optimise -> APK plus léger et plus rapide
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
}
