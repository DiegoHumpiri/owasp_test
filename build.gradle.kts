plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	// Pluging para generar el SBOM
	id("org.cyclonedx.bom") version "3.6.1"
}

group = "edu.lab"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Dependencia con vulnerabilidad
	compileOnly("org.apache.logging.log4j:log4j-core:2.26.1")
	// Dependencia con vulnerabilidad parcheada
	//compileOnly("org.apache.logging.log4j:log4j-core:2.25.5")
}

tasks.withType<Test> {
	useJUnitPlatform()
}


tasks.cyclonedxDirectBom {
    includeConfigs = listOf(
        "runtimeClasspath",
        "compileClasspath"
    )

    failOnUnresolvedDependencies = true
}
