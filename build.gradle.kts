// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.kotlin.parcelize) apply false
	alias(libs.plugins.compose.compiler) apply false
}

val publishedGroup = "io.github.salmanashraf"
val publishedVersion = providers
	.gradleProperty("LIB_VERSION")
	.orElse("0.0.1-SNAPSHOT")
	.get()

allprojects {
	group = publishedGroup
	version = publishedVersion
}
