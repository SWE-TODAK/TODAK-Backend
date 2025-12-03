# TODAK Backend 
🩺 병원 헬스케어 자동화 서비스 **TODAK** 의 백엔드 서버입니다. 
--- 
## 백엔드 개발 팀원 
- 동국대학교 컴퓨터공학전공 23학번 최희수
- 동국대학교 컴퓨터공학전공 23학번 최홍서
---
## Tech Stack 
- **Language:** Java 17
- **Framework:** Spring Boot
- **Database:** PostgreSQL(Supabase)
- **Storage:** AWS S3
<div> <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"/> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> <img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/> </div>

--- 

## Architecture

<img width="2376" height="1316" alt="supabase-schema-kkfxxteavjukwkrdtycc (2)" src="https://github.com/user-attachments/assets/2547ec87-de2e-4176-96db-ffd6c0e8bfa8" />


---

## Project Structure
```bash         
src/main/java/com.todak.api
 ├─ appointment      # 예약 생성·조회 도메인
 ├─ auth             # 로그인/인증 
 ├─ common           # 공통 응답, 예외 처리, 유틸
 ├─ config           # STT 요청 템플릿 및 애플리케이션 설정
 ├─ consultation     # 진료 생성·조회 도메인
 ├─ health           # 건강 지표 기록 관리 (추후 확장)
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra
 │    ├─ s3          # AWS S3 업로드/다운로드
 │    └─ ai          # AI 서버(STT/요약) 통신 클라이언트
 ├─ recording        # 녹음 인증·업로드·STT 요청 처리
 ├─ summary          # STT 결과 요약 도메인
 └─ user             # 사용자 관리
```

 ---
 
## Core Flows

### 🔐 Authentication
### 1) 카카오 로그인 (User Login) 
- **토큰 교환 & 유저 정보**: 백엔드는 프론트가 인가 코드를 사용해 카카오 서버에서 액세스 토큰을 받아서 주면, 이를 통해 고유 유저 ID를 조회
- **자동 회원가입/로그인**: DB 조회 후 신규 유저면 자동 회원가입, 기존 유저면 로그인 처리
- **JWT 발급**: 서비스 전용 JWT(Access Token)을 생성하여 클라이언트에 반환

### 2) API 요청 인증 (JWT Verification)
  - **헤더 검사**: API 요청 시 헤더의 Authorization: Bearer {Token} 확인
  - **필터링**: JwtAuthenticationFilter에서 토큰의 유효성 및 만료 여부 검증
  - **인증 처리**: 검증 통과 시 SecurityContext에 인증 정보를 저장하여 API 접근 허용

### 🎙 Recording & Summary Pipeline
### 1) 녹음 업로드 
- 클라이언트가 음성 파일 업로드
- 서버가 S3에 저장하고 Recording 생성

### 2) STT 요청 (Speech-to-Text)
- 서버가 S3에서 음성 파일 다운로드
- AI 서버에 파일 전달 → Whisper로 STT 수행
- STT 결과(transcript, duration)를 Recording에 저장

### 3) 요약 요청 (Summarization)
- STT 결과 텍스트를 AI 서버로 전달
- 요약 모델이 상담 요약 생성
- 결과를 Summary 엔티티로 저장

### 4) 진료(Consultation)와 연결
- Recording과 Summary는 Consultation에 연결되어 “한 번의 진료에 대한 음성 + STT + 요약” 패키지 완성

---
