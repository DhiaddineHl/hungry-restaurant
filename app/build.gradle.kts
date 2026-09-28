plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.hungry.restaurant.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hungry.restaurant.pos"
        // Sunmi T2/T3/D3 etc. ship Android 7.1+; keep a low floor for older fleet devices.
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Scheme AppAuth's RedirectUriReceiverActivity registers for the OAuth/OIDC
        // redirect (com.hungry.restaurant.pos:/oauth2redirect) — must match the
        // Keycloak client's registered redirect URI.
        manifestPlaceholders["appAuthRedirectScheme"] = "com.hungry.restaurant.pos"

        // The hungry-app API gateway. Defaults to the dev machine's current LAN IP
        // (192.168.95.214) that its docker-compose stack is actually reachable on — a
        // POS terminal on the same Wi-Fi reaches the gateway on :8082. Override per-build
        // with `-PapiBaseUrl=https://...` (e.g. a deployed gateway) without editing source;
        // this default will need updating again if the dev machine's IP changes (DHCP).
        val apiBaseUrl = (project.findProperty("apiBaseUrl") as String?)
            ?: "http://172.20.10.3:8082/"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        // Keycloak, reached DIRECTLY (not through the gateway - same reasoning as the
        // customer app's EXPO_PUBLIC_KEYCLOAK_URL: the gateway only routes to hungry-app,
        // so /realms/... isn't one of its routes). Same dev-machine LAN IP as apiBaseUrl,
        // on :8081. Override with `-PkeycloakBaseUrl=https://...` for a deployed Keycloak
        // without editing source.
        val keycloakBaseUrl = (project.findProperty("keycloakBaseUrl") as String?)
            ?: "http://172.20.10.3:8081"
        buildConfigField("String", "KEYCLOAK_BASE_URL", "\"$keycloakBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.sunmi.printerlibrary)

    implementation(libs.openid.app.auth)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.browser)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    debugImplementation(libs.androidx.ui.tooling)
}
