import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.parcelize)
	id("kotlin-kapt")
	id("maven-publish")
	id("signing")
}

val releaseGroupId = project.group.toString()
val releaseVersion = project.version.toString()
val centralPortalRepoDir = layout.buildDirectory.dir("central-component/repository")
val centralPortalRepoDirFile = centralPortalRepoDir.map { it.asFile }

android {
	namespace = "com.sa.aichatlib"
	compileSdk = 36

	defaultConfig {
		minSdk = 24
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.composeUi.get()
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
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	publishing {
		singleVariant("release") {
			withSourcesJar()
		}
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.coroutines.android)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.okhttp)
	implementation(libs.retrofit)
	implementation(libs.gson.converter)
	implementation(libs.gson)
	implementation(libs.json)
	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	kapt(libs.room.compiler)
	implementation(libs.lifecycle.viewmodel.ktx)
	implementation(libs.lifecycle.viewmodel.compose)
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	testImplementation(libs.junit)
	testImplementation(libs.mockwebserver)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
	arguments {
		arg("room.schemaLocation", "$projectDir/schemas")
		arg("room.incremental", "true")
	}
}

/* ---------------------------------------------------------
   2. EMPTY JAVADOC JAR (Maven Central requirement)
--------------------------------------------------------- */
val emptyJavadocDir = layout.buildDirectory.dir("emptyJavadoc")

val createEmptyJavadoc by tasks.registering {
	doLast {
		emptyJavadocDir.get().asFile.mkdirs()
	}
}

val javadocJar by tasks.registering(Jar::class) {
	dependsOn(createEmptyJavadoc)
	archiveClassifier.set("javadoc")
	from(emptyJavadocDir)
}

/* ---------------------------------------------------------
   3. PUBLISHING — MUST BE inside afterEvaluate()
--------------------------------------------------------- */

afterEvaluate {

	publishing {
		publications {
			create<MavenPublication>("release") {

				groupId = releaseGroupId
				artifactId = "aichatlib"
				version = releaseVersion

				// Use the release component (includes AAR + dependencies)
				from(components["release"])

				// Only add javadoc jar (sources is already included by component)
				artifact(javadocJar.get())

				pom {
					name.set("AI Chat Android SDK")
					description.set("A multi-provider AI chat SDK.")
					url.set("https://github.com/salmanashraf/android-ai-chat-sdk")

					licenses {
						license {
							name.set("MIT License")
							url.set("https://opensource.org/licenses/MIT")
						}
					}

					developers {
						developer {
							id.set("salmanashraf")
							name.set("Salman Ashraf")
							email.set("salmanashraf.12@gmail.com")
						}
					}

					scm {
						url.set("https://github.com/salmanashraf/android-ai-chat-sdk")
						connection.set("scm:git:git://github.com/salmanashraf/android-ai-chat-sdk.git")
						developerConnection.set("scm:git:ssh://git@github.com/salmanashraf/android-ai-chat-sdk.git")
					}
				}
			}
		}

		repositories {
			maven {
				name = "CentralPortal"
				url = uri(centralPortalRepoDirFile.get())
			}
			maven {
				name = "OSSRH"
				val isSnapshot = releaseVersion.endsWith("-SNAPSHOT")
				url = uri(
					if (isSnapshot) {
						"https://ossrh-staging-api.central.sonatype.com/content/repositories/snapshots/"
					} else {
						"https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
					}
				)
				credentials {
					username = project.findProperty("ossrhUsername") as String?
					password = project.findProperty("ossrhPassword") as String?
				}
			}
		}
	}

	tasks.withType<PublishToMavenRepository>().configureEach {
		onlyIf {
			val hasCredentials = project.findProperty("ossrhUsername") != null &&
				project.findProperty("ossrhPassword") != null

			repository.name != "OSSRH" || hasCredentials
		}
	}

	signing {
		useGpgCmd()
		sign(publishing.publications["release"])
	}

	val publishCentralPortal = tasks.named("publishReleasePublicationToCentralPortalRepository")

	tasks.register<Zip>("bundleCentralComponent") {
		group = "publishing"
		description = "Packages the Central Portal component zip with checksums/signatures."
		dependsOn(publishCentralPortal)
		from(centralPortalRepoDirFile)
		destinationDirectory.set(layout.buildDirectory.dir("central-component"))
		archiveFileName.set("aichatlib-${releaseVersion}-component.zip")
	}
}
