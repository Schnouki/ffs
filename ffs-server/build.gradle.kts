import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    alias(libs.plugins.sqldelight)
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinx.benchmark)
}

sourceSets.create("benchmark")

dependencies {
    implementation(project(":ffs-shared:db"))
    implementation(project(":ffs-shared:env"))
    implementation(project(":ffs-shared:endpoints"))
    implementation(project(":ffs-shared:rule"))
    implementation(project(":ffs-shared:session-header"))
    implementation(project(":ffs-shared:sse"))
    implementation(project(":ffs-shared:validators"))

    implementation(libs.bundles.server.ktor)
    implementation(libs.bundles.server.database)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.resources)
    testImplementation(libs.kotlinx.benchmark.runtime)

    val benchmarkImplementation by configurations.getting
    benchmarkImplementation(sourceSets.main.get().let { it.output + it.compileClasspath })
    benchmarkImplementation(libs.kotlinx.benchmark.runtime)
}

application {
    mainClass.set("doist.ffs.ApplicationKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "io.ktor.server.cio.EngineMain"))
        }
    }

    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
}

sqldelight {
    database("Database") {
        packageName = "doist.ffs"
        dialect = System.getenv("SQLDELIGHT_DIALECT")
            ?: project.properties["sqldelightDialect"]?.toString()
                ?: "sqlite:3.18"
    }
}

benchmark {
    configurations.named("main") {
        warmups = 2
        iterations = 3
        iterationTime = 5
        reportFormat = "csv"
        advanced("nativeGCAfterIteration", "true")
    }

    targets {
        register("benchmark")
    }
}
