plugins {
    id("signing")
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven(uri("https://papermc.io/repo/repository/maven-public/"))
    maven(uri("https://maven.plugily.xyz/releases"))
    maven(uri("https://maven.plugily.xyz/snapshots"))
    maven(uri("https://repo.maven.apache.org/maven2/"))
}

dependencies {
    implementation("plugily.projects:MiniGamesBox-Classic:1.3.8") { isTransitive = false }
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly(files("lib/CorpseReborn.jar"))
}

group = "plugily.projects"
version = "2.0.6-SNAPSHOT2"
description = "MurderMystery"

java {
    withJavadocJar()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("plugily.projects.minigamesbox", "plugily.projects.murdermystery.minigamesbox")
        relocate("com.zaxxer.hikari", "plugily.projects.murdermystery.database.hikari")
        minimize()
    }

    processResources {
        filesMatching("**/plugin.yml") {
            expand(project.properties)
        }
    }

    javadoc {
        options.encoding = "UTF-8"
    }

}

publishing {
    repositories {
        maven {
            name = "Releases"
            url = uri("https://maven.plugily.xyz/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "Snapshots"
            url = uri("https://maven.plugily.xyz/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
