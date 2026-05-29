plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.parcelize)
	alias(libs.plugins.compose.compiler)
}

android {
	namespace = "com.sa.sampleapp"
	compileSdk = 36

	defaultConfig {
		applicationId = "com.sa.sampleapp"
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
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
	kotlinOptions {
		jvmTarget = "21"
	}

	buildFeatures {
		compose = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(project(":ai-chat-lib"))
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.lifecycle.viewmodel.compose)
	implementation(libs.activity.compose)
	implementation(libs.okhttp)
	implementation(libs.kotlinx.serialization.json)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
