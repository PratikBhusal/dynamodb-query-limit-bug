import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.gitlab.arturbosch.detekt.Detekt
import kotlin.io.path.Path
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Relevant links:
//
// - https://kotlinlang.org/docs/whatsnew18.html#updated-jvm-compilation-target
plugins {
    id("java-library")

    alias(libs.plugins.delta.coverage)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.task.tree)
}

repositories {
    // Use Maven Local when working on in-development versions/changes.
    mavenLocal()
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

// Maven Scope -> Gradle Configuration Translation/Overview
// =============================================================================
// `compile` -> `implementation` or `api`. Prefer `implementation` unless you
//              have good reason to expose the dependency to consumers.
//
// `test`    -> `testImplementation`
//
// `runtime` -> `runtimeOnly`
//
// See:
// - https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
// - https://reflectoring.io/maven-scopes-gradle-configurations
// - https://kotlinlang.org/docs/gradle-configure-project.html#dependency-types
dependencies {
    // Note: We want to expose these to dependents because some of our methods
    // will return objects found within these libraries.
    api(libs.arrow.core)

    runtimeOnly(libs.log4j.core)

    implementation(libs.bundles.jvm.shared.logging.implementation)
    implementation(libs.dynamodb.enhanced.client)
    implementation(libs.fasterxml.uuid.v7.generator)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.aws.dynamodb.local)
    testImplementation(libs.bundles.jvm.shared.testing)
}

kotlin {
    // Apply a specific Java toolchain to ease working on different
    // environments. Because we specify the kotlin JVM toolchain, the java
    // toolchains will use the same version
    //
    // See:
    // - https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())

    compilerOptions {
        // https://github.com/JetBrains/kotlin/blob/12f5819170c546cbf0cbae4824f06d41d98c3223/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt
        // https://github.com/JetBrains/kotlin/blob/12f5819170c546cbf0cbae4824f06d41d98c3223/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/CommonCompilerArguments.kt
        freeCompilerArgs.addAll(
            // https://kotlinlang.org/docs/compiler-reference.html#progressive
            "-progressive",
            // https://kotlinlang.org/docs/java-interop.html#compiler-configuration
            // https://docs.spring.io/spring-framework/reference/languages/kotlin/null-safety.html
            "-Xjsr305=strict",
            // Add type annotations into JVM bytecode
            //
            // See:
            // - https://kotlinlang.org/docs/whatsnew14.html#type-annotations-in-the-jvm-bytecode
            //
            // TODO: Determine if we still need this for Kotlin 1.9+ and/or Java 21+
            "-Xemit-jvm-type-annotations",
            // When calling Java code, type arguments and type parameters annotations
            //
            // See:
            // - https://kotlinlang.org/docs/java-interop.html#annotating-type-arguments-and-type-parameters
            "-Xtype-enhancement-improvements-strict-mode",
            // https://kotlinlang.org/docs/whatsnew1520.html#support-for-jspecify-nullness-annotations
            "-Xjspecify-annotations=strict",
            // Match JVM assertion behavior.
            //
            // See:
            // - https://youtrack.jetbrains.com/issue/KT-22292
            // - https://publicobject.com/2019/11/18/kotlins-assert-is-not-like-javas-assert/
            "-Xassertions=jvm",
            // Enable jvm-default behavior without `DefaultImpls` static class
            //
            // See:
            // https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces/
            "-Xjvm-default=all",
        )
    }
}

// https://detekt.dev/docs/gettingstarted/gradle#kotlin-dsl-3
detekt {
    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config.setFrom(Path(rootDir.canonicalPath, ".config", "detekt", "detekt.yml"))
}

configure<DeltaCoverageConfiguration> {
    coverage.engine = CoverageEngine.INTELLIJ

    diffSource.git.apply {
        compareWith("refs/remotes/origin/main")
        useNativeGit = true
    }

    reports {
        html = true
        xml = true
    }
}

// See:
// -
// https://github.com/Kotlin/kotlinx-kover/blob/742d2abe7cd4ee6f3b27b7db628a2d80b013904e/kover-gradle-plugin/docs/configuring.md
koverReport { defaults { html { onCheck = true } } }

tasks {
    // Make Kotlin Gradle plugin work with Java Platform Module System work.
    //
    // TODO: Setup what the module name should be
    //
    // See:
    // - https://kotlinlang.org/docs/gradle-configure-project.html#configure-with-java-modules-jpms-enabled
    // named("compileJava", JavaCompile::class.java) {
    //     options.compilerArgumentProviders.add(
    //         CommandLineArgumentProvider {
    //             // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work

    //             listOf("--patch-module", "YOUR_MODULE_NAME=${sourceSets["main"].output.asPath}")
    //         },
    //     )
    // }

    register("docs") {
        group = "Documentation"
        description = "Generates documentation"

        dependsOn(dokkaHtml)
    }

    register("format") {
        group = "Verification"
        description = "Format code"

        dependsOn(ktlintFormat)
    }

    dokkaHtml {
        doLast {
            println("[dokka]: file://${this@dokkaHtml.outputs.files.asPath}/index.html")
        }
    }

    javadoc { dependsOn(dokkaHtml) }

    test {
        environment("DDB_LOCAL_TELEMETRY", 0)
        finalizedBy(koverHtmlReport, deltaCoverage)
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }

    withType<Detekt>().configureEach {
        dependsOn(ktlintCheck)
    }
}
