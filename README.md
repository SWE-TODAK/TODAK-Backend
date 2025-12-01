# TODAK Backend 🩺

병원 헬스케어 자동화 서비스 **TODAK** 의 백엔드 서버입니다.  

---

## 🏗 Tech Stack

- Language: **Java17** 
- Framework: **Spring Boot**
- Build: **Gradle** – `build.gradle.kts`
- DB: (예시) **PostgreSQL (Supabase)** 
- Storage: **AWS S3** (녹음 파일 업로드)
- Infra:
  - 홍서띠가 쓰기



---

## 🔌 주요 모듈 구조
src/main/java/com.todak.api
 ├─ appointment      # 진료 날짜·시간 선택 및 예약 후보 조회
 ├─ auth             # 로그인/인증 (추후 확장)
 ├─ common           # 
 ├─ config           # stt request 템플릿 저장
 ├─ consultation     # 진료 생성/조회 도메인
 ├─ health           # 건강 지표 기록 관리(추후 확장 예정)
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra         
 │   ├─ s3        
 │   └─ ai         
 ├─ recording        # 녹음 인증/업로드/stt 요청
 ├─ summary          # STT 결과 요약 
 └─ user             # 사용자 관리


 



# Todak Backend

**TodAi**는 사용자가 음성으로 감정을 기록하면 인공지능 모델이 음성과 텍스트 기반으로 분석한 결과를 종합하여, 사용자가 자신의 감정을 돌아보고 관리할 수 있도록 돕는 감정 분석 기반 일기 서비스 프로젝트입니다.  
본 프로젝트는 텍스트와 음성을 결합한 멀티모달 감정 분석 모델을 구현하여 정밀한 감정 판단을 가능하게 하고, 개인 맞춤형 정서 피드백 서비스로 확장될 수 있는 기반을 마련합니다.  
본 레포지토리는 TodAi 서비스의 **백엔드(Spring Boot)** 서버를 위한 코드입니다.

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

프로젝트 업데이트 전에는 **Django**로 전체 백엔드를 구성했으나, 현재는 **Spring Boot**와 **Django** 백엔드를 구성하여 각각의 역할을 분리하여 사용합니다.

<img src="https://github.com/user-attachments/assets/4624c478-c9a5-4d5e-9cff-78954c901737"  width="700"/>
  

---

##  Project Structure
          
```bash          
src/main/java/com.todak.api
 ├─ appointment      # 진료 날짜·시간 선택 및 예약 후보 조회
 ├─ auth             # 로그인/인증 (추후 확장)
 ├─ common           # 
 ├─ config           # stt request 템플릿 저장
 ├─ consultation     # 진료 생성/조회 도메인
 ├─ health           # 건강 지표 기록 관리(추후 확장 예정)
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra         
 │   ├─ s3        
 │   └─ ai         
 ├─ recording        # 녹음 인증/업로드/stt 요청
 ├─ summary          # STT 결과 요약 
 └─ user             # 사용자 관리
 
```
## Core Flows

### 🔐 Authentication



### 🎙 Recording Pipeline

파일 → S3 업로드
S3 객체 → MultipartFile 변환
AI 서버 STT 요청 → transcript 저장
요약 요청 → summary 저장

Recording status:
RECORDED → TRANSCRIBED → SUMMARIZED

---



