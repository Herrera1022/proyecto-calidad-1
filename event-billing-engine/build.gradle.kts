plugins {
    java
    application
    // Analisis estatico con SonarQube Cloud
    id("org.sonarqube") version "7.5.0.8588"
}

group = "com.events.billing"
version = "1.0.0"

java {
    // Java moderno (17+). El toolchain hace que Gradle use exactamente esta version.
    toolchain {
        // Java 21 (LTS): es el JDK instalado en el equipo del analisis local.
        languageVersion = JavaLanguageVersion.of(21)
    }
}



application {
    mainClass = "com.events.billing.Main"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5 (agregador: API + motor + parametros). Scope de test.
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

    // Necesario en las versiones recientes de Gradle para lanzar la Plataforma JUnit.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    // Activa la Plataforma JUnit 5 (sin esto Gradle no encuentra los tests).
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        // Muestra en consola los println de los performance tests.
        showStandardStreams = true
    }
}

// ===================== SonarQube Cloud =====================
// El token NO va aqui: el plugin lo lee de la variable de entorno SONAR_TOKEN.
sonar {
    properties {
        property("sonar.projectKey", "Herrera1022_proyecto-calidad-1")
        property("sonar.organization", "herrera1022")
        property("sonar.host.url", "https://sonarcloud.io")
        // Etapa del analisis: aparece en la pestana Activity de SonarCloud.
        property("sonar.projectVersion", "1.0")
    }
}
