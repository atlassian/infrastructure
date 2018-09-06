val kotlinVersion = "1.2.30"

plugins {
    kotlin("jvm").version("1.2.30")
    `java-library`
    id("com.atlassian.performance.tools.gradle-release").version("0.4.1")
}

configurations.all {
    resolutionStrategy {
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
            }
        }
    }
}

dependencies {
    api("com.atlassian.performance.tools:ssh:[1.0.0,2.0.0)")
    api("com.atlassian.performance.tools:jira-actions:[2.0.0,3.0.0)")
    api("com.atlassian.performance.tools:virtual-users:[1.0.0,3.0.0)")

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
}

fun log4j(
    vararg modules: String
): List<String> = modules.map { module ->
    "org.apache.logging.log4j:log4j-$module:2.10.0"
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.9"
    distributionType = Wrapper.DistributionType.ALL
}
