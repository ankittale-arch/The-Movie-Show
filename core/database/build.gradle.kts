plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ankitt.themovieshow.core.database"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    // Room's in-memory database needs a real SQLite implementation, which the plain JVM unit
    // test runner doesn't provide — Robolectric supplies one, so DAO/migration tests run fast as
    // local JVM tests instead of needing an emulator.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // MigrationTestHelper loads each version's exported schema JSON as an Android asset (see
    // room.schemaLocation below) rather than reading the file directly, so the schemas/ directory
    // is wired in here as a test asset source so Robolectric's AssetManager can find it too.
    sourceSets {
        getByName("test") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

// Room validates every @Entity/@Dao against a schema snapshot on each build and writes that
// snapshot as JSON under schemas/. Those files are committed to VCS: they are the only reliable
// record of what shipped in each database version, and Room's MigrationTestHelper (Phase 10)
// reads them directly to test that a migration from version N to N+1 actually produces the
// version N+1 schema.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
}
