plugins {
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
}

group = "com.alikemal"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1")
        bundledPlugin("com.intellij.java")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

intellijPlatform {
    pluginConfiguration {
        changeNotes.set("""
            <ul>
                <li>Modern Kotlin rewrite with Clean Code architecture.</li>
                <li>Reused HttpClient singleton inside companion object.</li>
                <li>Improved status bar integration and error handling.</li>
            </ul>
        """.trimIndent())
    }
}

tasks.test {
    useJUnitPlatform()
}
