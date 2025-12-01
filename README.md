# TODAK Backend 🩺

병원 헬스케어 자동화 서비스 **TODAK** 의 백엔드 서버입니다.  

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

<div>
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"/> 
<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> 
<img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>
</div>


---

## Architecture

나중에
  

---

##  Project Structure
          
```bash          
src/main/java/com.todak.api
 ├─ appointment      # 진료 날짜·시간 선택 및 예약 후보 조회
 ├─ auth             # 로그인/인증 (추후 확장)
 ├─ common           # 공통 응답, 예외 처리, 유틸
 ├─ config           # STT 요청 템플릿 및 애플리케이션 설정
 ├─ consultation     # 진료(상담) 생성·조회 도메인
 ├─ health           # 건강 지표 기록 관리 (추후 확장)
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra
 │    ├─ s3          # AWS S3 업로드/다운로드
 │    └─ ai          # AI 서버(STT/요약) 통신 클라이언트
 ├─ recording        # 녹음 인증·업로드·STT 요청 처리
 ├─ summary          # STT 결과 요약 도메인
 └─ user             # 사용자 관리
 
```
## Core Flows

### 🔐 Authentication



### 🎙 Recording & Summary Pipeline

1) 녹음 업로드
   - 클라이언트가 음성 파일 업로드
   - 서버가 S3에 저장하고 Recording 생성

2) STT 요청 (Speech-to-Text)
   - 서버가 S3에서 음성 파일 다운로드
   - AI 서버에 파일 전달 → Whisper로 STT 수행
   - STT 결과(transcript, duration)를 Recording에 저장

3) 요약 요청 (Summarization)
   - STT 결과 텍스트를 AI 서버로 전달
   - 요약 모델이 상담 요약 생성
   - 결과를 Summary 엔티티로 저장

4) 진료(Consultation)와 연결
   - Recording과 Summary는 Consultation에 연결되어
     “한 번의 진료에 대한 음성 + STT + 요약” 패키지 완성


---



