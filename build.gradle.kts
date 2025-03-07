import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") apply true
    id("io.github.gradle-nexus.publish-plugin") apply true
    id("io.gitlab.arturbosch.detekt")
}

allprojects {
    apply(from = rootProject.file("buildScripts/gradle/checkstyle.gradle.kts"))

    if (this.name != "exposed-tests" && this.name != "exposed-bom" && this != rootProject) {
        apply(from = rootProject.file("buildScripts/gradle/publishing.gradle.kts"))
    }
}

val reportMerge by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.buildDir.resolve("reports/detekt/exposed.xml"))
}

subprojects {
    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.21.0")
    }
    tasks.withType<Detekt>().configureEach detekt@{
        enabled = this@subprojects.name !== "exposed-tests"
        finalizedBy(reportMerge)
        reportMerge.configure {
            input.from(this@detekt.xmlReportFile)
        }
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("exposed.sonatype.user"))
            password.set(System.getenv("exposed.sonatype.password"))
            useStaging.set(true)
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}
