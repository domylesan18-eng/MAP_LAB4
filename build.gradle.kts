plugins {
    java
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

// compileaza pentru Java 17, ca sa ruleze pe orice JDK >= 17 (si pe cel din IntelliJ)
tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

application {
    mainClass = "org.example.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    // driverul JDBC pentru PostgreSQL (folosit de repository-urile DATABASE)
    implementation("org.postgresql:postgresql:42.7.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// trimite tastatura catre program cand rulezi prin Gradle
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}
