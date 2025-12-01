# 1. 빌드 환경 설정 (Java 17이 필요하므로 JDK 17을 사용)
FROM eclipse-temurin:17-jdk-focal as builder

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle Wrapper 및 설정 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .


COPY src src

RUN chmod +x ./gradlew

RUN ./gradlew clean build -x test

# 6. 최종 실행 환경 설정 (JRE만 포함된 경량 이미지 사용)
FROM eclipse-temurin:17-jre-focal

# 7. 실행 환경 설정
WORKDIR /app

# 8. JAR 파일 복사 (빌드 단계에서 만든 파일만 가져옴)
COPY --from=builder /app/build/libs/*.jar app.jar

# 9. 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 10. 컨테이너 실행 명령 (실행 시 -D 옵션으로 환경변수 주입 필요)
ENTRYPOINT ["java", "-jar", "app.jar"]