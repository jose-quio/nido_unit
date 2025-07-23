plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")

	//PostgreSQl Driver
	implementation("org.postgresql:postgresql:42.7.3")

	// Hibernate Dialect (opcional si quieres soporte extendido)
	implementation("org.hibernate.orm:hibernate-community-dialects:6.6.11.Final")

	//implementation("org.xerial:sqlite-jdbc:3.42.0.0")
	implementation("org.hibernate.validator:hibernate-validator")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("javax.xml.bind:jaxb-api:2.3.0")

	//Pdf y Correo
	implementation("com.itextpdf:itext7-core:7.2.3")
	implementation("org.springframework.boot:spring-boot-starter-mail")

	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
