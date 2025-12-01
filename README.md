# TODAK Backend 🩺

실시간 소아과 진료 예약 및 비대면 진료 서비스 **TODAK** 의 백엔드 서버입니다.  
병원 / 의사 정보 조회, 진료 예약, 녹음 업로드, AI 음성 인식(STT)·요약 등의 기능을 제공합니다.

---

## 🏗 Tech Stack

- Language: **Java** (JDK 17 기준)
- Framework: **Spring Boot**
- Build: **Gradle (Kotlin DSL)** – `build.gradle.kts`
- DB: (예시) **MySQL** / **RDS** 등
- Storage: **AWS S3** (녹음 파일 업로드)
- Infra:
  - 내부 **AI 서버** (Whisper STT + 요약 모델)
  - Docker / Dockerfile 로 컨테이너 빌드
- 기타:
  - Spring Data JPA
  - Spring Web / Validation
  - (선택) Spring Security, JWT 인증 등

> 실제 사용 중인 DB, 인증 방식은 팀 설정에 맞게 수정해 주세요.

---

## 🔌 주요 모듈 구조

> 패키지명은 실제 코드 기준으로 맞춰서 수정하면 됩니다.

```text
src
 └─ main
    ├─ java
    │   └─ com.todak.api
    │       ├─ TodakApiApplication.kt (or .java)
    │       ├─ global            # 공통 예외, 공통 응답, 설정
    │       ├─ hospital          # 병원 / 의사 / 진료과 조회
    │       ├─ consultation      # 진료 예약/상담 도메인
    │       ├─ recording         # 녹음, 녹음 상태 관리
    │       ├─ summary           # STT 결과 요약
    │       └─ infra
    │           ├─ s3            # S3UploaderService 등 S3 연동
    │           └─ ai            # AiClient: STT/요약 AI 서버 연동
    └─ resources
        ├─ application.yml       # 프로파일 공통 설정
        ├─ application-local.yml # 로컬 개발용 설정
        └─ application-prod.yml  # 운영 설정

## 📁 대표 패키지 설명

### `infra.s3`
AWS S3에 음성 파일을 업로드/다운로드하는 인프라 계층입니다.

#### **S3UploaderService**
- `upload(MultipartFile file): String`  
  → 파일을 S3에 업로드하고 **저장된 key(경로)** 를 반환합니다.
- `downloadAsMultipartFileByKey(String key): MultipartFile`  
  → S3에서 객체를 읽어와 `MockMultipartFile` 형태로 반환합니다.  
    이 반환값을 그대로 `AiClient` 에 넘겨 **Whisper STT 요청**에 사용합니다.

---

### `infra.ai`
내부 AI 서버(Whisper STT + 요약 모델)와 HTTP 통신을 처리하는 계층입니다.

#### **AiClient**
- `requestStt(Long recordingId, Long consultationId, MultipartFile file)`  
  → S3에서 받은 음성 파일을 AI 서버로 전송하여 STT 수행
- `requestSummary(Long recordingId, String transcript)`  
  → STT 결과 텍스트를 요약 모델에 전달하여 요약 결과 생성

**사용 설정 값**
- `ai.server.base-url`  
- `ai.server.internal-key`  

(↑ `application.yml` 에서 주입받아 RestTemplate/WebClient로 호출)

---

### `recording`
녹음 관리 기능을 담당하는 핵심 도메인입니다.

#### **Recording 엔티티**
- 필드 구조:
  - `id`
  - `consultationId`
  - `s3Key`
  - `transcript`
  - `status` (예: `RECORDED`, `TRANSCRIBED`)

#### **RecordingController**
- 녹음 시작 / 종료
- S3 업로드
- STT 요청
- 요약(summary) 요청  
→ 프론트엔드와 직접 맞닿는 API 제공

#### **RecordingService**
- S3 업로드 / S3 다운로드 처리
- STT 요청 시 AiClient 호출
- Recording 상태 변경 관리

---

### `hospital`
병원 / 의사 / 진료과 정보 조회 도메인입니다.

#### **HospitalController**
- 병원 목록 조회
- 병원 상세 조회
- 병원 검색

#### **HospitalService**
- DB에서 병원, 의사, 진료과 정보 조회
- DTO 변환 및 데이터 가공 처리

---

### `consultation`
사용자의 진료 예약 및 상담 흐름을 관리하는 도메인입니다.

- 진료(Consultation) 생성 / 조회 기능 제공
- 생성된 Consultation 과 Recording 연동  
  → 녹음/요약 결과를 특정 예약 건에 귀속하여 관리
