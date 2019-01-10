val kotlinVersion = "1.2.70"

plugins {
    kotlin("jvm").version("1.2.70")
    `java-library`
    id("com.atlassian.performance.tools.gradle-release").version("0.4.3")
}

configurations.all {
    resolutionStrategy {
        activateDependencyLocking()
        failOnVersionConflict()
        eachDependency {
            when (requested.module.toString()) {
                "com.google.guava:guava" -> useVersion("23.6-jre")
                "org.apache.httpcomponents:httpclient" -> useVersion("4.5.5")
                "org.apache.httpcomponents:httpcore" -> useVersion("4.4.9")
                "org.codehaus.plexus:plexus-utils" -> useVersion("3.1.0")
                "org.slf4j:slf4j-api" -> useVersion("1.8.0-alpha2")
                "com.google.code.gson:gson" -> useVersion("2.8.2")
                "org.jsoup:jsoup" -> useVersion("1.10.2")
                "org.jetbrains:annotations" -> useVersion("15.0")
                "com.google.code.findbugs:jsr305" -> useVersion("3.0.2")
                "org.apache.commons:commons-compress" -> useVersion("1.9")
            }
            when (requested.group) {
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
            }

        }
    }
}

dependencies {
    api("com.atlassian.performance.tools:ssh:[2.0.0,3.0.0)")
    api("com.atlassian.performance.tools:jira-actions:[2.0.0,4.0.0)")
    api("com.atlassian.performance.tools:virtual-users:[3.4.0,4.0.0)")

    implementation("com.atlassian.performance.tools:io:[1.0.0,2.0.0)")
    implementation("com.atlassian.performance.tools:concurrency:[1.0.0,2.0.0)")
    implementation("com.atlassian.performance.tools:jvm-tasks:[1.0.0,2.0.0)")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
    implementation("com.google.guava:guava:23.6-jre")
    implementation("org.apache.httpcomponents:httpclient:4.5.5")
    implementation("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-impl-maven:3.1.3")
    log4j(
        "api",
        "core",
        "slf4j-impl"
    ).forEach { implementation(it) }

    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-library:1.3")
    testCompile("org.testcontainers:testcontainers:1.9.1")
    testCompile("org.assertj:assertj-core:3.11.1")
}

fun log4j(
    vararg modules: String
): List<String> = modules.map { module ->
    "org.apache.logging.log4j:log4j-$module:2.10.0"
}

tasks.getByName("test", Test::class).apply {
    filter {
        exclude("**/*IT.class")
    }
}

val testIntegration = task<Test>("testIntegration") {
    filter {
        include("**/*IT.class")
    }
    maxParallelForks = 4
}

tasks["check"].dependsOn(testIntegration)

task<Wrapper>("wrapper") {
    gradleVersion = "4.9"
    distributionType = Wrapper.DistributionType.ALL
}
