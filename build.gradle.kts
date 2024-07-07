plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "org.flmelody"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("io.netty:netty-all:4.1.111.Final")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    testImplementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

tasks.processResources {
    files("META-INF/netcell.properties") {
        expand(mapOf("projectVersion" to project.version))
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "org.flmelody"
            artifactId = "netcell"
            this.version = version
            from(components["java"])

            pom {
                name.set("netcell")
                description.set("Another MQTT broker")
                url.set("https://github.com/Flmelody/netcell")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("esotericman")
                        name.set("esotericman")
                    }
                }
                scm {
                    connection.set("scm:git:git:github.com/Flmelody/netcell.git")
                    developerConnection.set("scm:git:ssh://github.com/Flmelody/netcell.git")
                    url.set("https://github.com/Flmelody/netcell.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = project.properties["username"] as String?
                password = project.properties["password"] as String?
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as? StandardJavadocDocletOptions)?.addBooleanOption("html5", true)
    }
}