plugins {
	java
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
}

group = "com.todak"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// 1. 웹 & DB 필수 도구
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// 2. 보안 & 인증 (Security + JWT 열쇠)
	implementation("org.springframework.boot:spring-boot-starter-security")
	// JWT 라이브러리 (최신 표준 0.12.x)
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

	// 3. AWS S3 (파일 업로드 배관)
	// Spring Boot 3.x 호환 공식 라이브러리
	implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")

	// 4. 유틸리티 (Lombok, API 문서 자동화)
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	// Swagger (API 명세서 자동 생성)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// 5. DB 드라이버 & 테스트 도구
	runtimeOnly("org.postgresql:postgresql") // Supabase(PostgreSQL)용
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework:spring-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}