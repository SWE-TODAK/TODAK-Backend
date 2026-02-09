# 1. Base 이미지 설정
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너 안으로 복사
# build.gradle.kts 설정에 따라 생성된 jar 파일명을 맞춤
COPY build/libs/*.jar app.jar

# 4. 환경 변수 설정 (Spring Profile을 운영모드로 실행)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]