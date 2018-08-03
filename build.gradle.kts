plugins {
    kotlin("jvm").version(Versions.kotlin)
    maven
}

maven {
    group = "com.atlassian.test.performance"
    version = "0.0.1-SNAPSHOT"
}

dependencies {
    compile(Libs.io)
    compile(Libs.concurrency)
    compile(Libs.tasks)
    compile(Libs.ssh)
    compile(Libs.json)
    compile(Libs.jiraActions)
    compile(Libs.kotlinStandard)
    compile(Libs.guava)
    compile("org.apache.httpcomponents:httpclient:4.5.5")
    compile("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-impl-maven:3.1.3")
    Libs.log4jCore().forEach { compile(it) }

    testCompile(Libs.junit)
    testCompile(Libs.hamcrest)
}