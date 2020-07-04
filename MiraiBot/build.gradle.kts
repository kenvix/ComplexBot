plugins {
    java
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    //id("org.beryx.jlink") version "2.17.2"
    id("nu.studer.jooq") version "4.1"
    id("java-library")
}

val ktor_version: String by project
val kotlin_version: String by project
val kotlinx_coroutines_version: String by project
val logback_version: String by project
val miraiCoreVersion: String by project

group = "com.kenvix"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    google()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://repo.spring.io/libs-milestone")
    maven(url = "https://repo.spring.io/plugins-release")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinx_coroutines_version")

    implementation("io.github.cdimascio:java-dotenv:5.1.4")
    implementation("org.apache.thrift:libthrift:0.13.0")

    implementation(group = "commons-cli", name = "commons-cli", version = "1.4")
    implementation(group = "org.yaml", name = "snakeyaml", version = "1.21")
    implementation(fileTree("libs"))

    implementation("com.squareup.okhttp3:okhttp:3.5.0")
    implementation("com.google.code.gson:gson:2.8.5")

    testImplementation("junit", "junit", "4.12")

    // https://mvnrepository.com/artifact/javax.persistence/javax.persistence-api
    implementation(group = "javax.persistence", name = "javax.persistence-api", version = "2.2")
    implementation(group = "org.jooq", name = "jooq", version = "3.11.5")
    implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.25.2")
    implementation(group = "mysql", name = "mysql-connector-java", version = "8.0.17")
    implementation(group = "org.apache.commons", name = "commons-dbcp2", version = "2.7.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.0.0")

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")

    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-network:$ktor_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")

    implementation("net.mamoe:mirai-core:$miraiCoreVersion")
    // implementation("net.mamoe:mirai-core-qqandroid:$miraiCoreVersion")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    //ahocorasick 字符串关键词匹配算法
    implementation("org.ahocorasick:ahocorasick:0.4.0")


    implementation("com.google.zxing:core:3.4.0")
    implementation("com.google.zxing:javase:3.4.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}