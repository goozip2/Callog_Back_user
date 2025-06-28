# 👤 User Service

### 📌 서비스 개요  
회원가입, 로그인, 사용자 인증 및 기본 사용자 정보를 관리하는 서비스입니다.  
사용자의 `아이디`, `이메일`, `닉네임`, `비밀번호` 정보를 담당하며,  
**JWT 발급**을 수행합니다.

---

## ✅ 주요 기능

- 회원가입 (`/user/register`)
- 로그인 및 JWT 발급 (`/user/login`)
- Access Token 재발급 (`/user/refresh`)
- 로그아웃

---

## 🧱 도메인 모델

| 필드명 | 설명 |
|--------|------|
| `id` | 사용자 고유 ID (PK) |
| `username` | 사용자 이메일 (로그인 ID) |
| `password` | 암호화된 비밀번호 |
| `nickname` | 사용자 닉네임 |

---

## 🔐 인증 처리 흐름

- 로그인 시 사용자 정보 검증 후 **Access Token / Refresh Token 발급**
- response에 Token 포함시켜 전달

---

## 🔗 외부 서비스 연동

- 회원가입 시 입력된 **신체 정보**를 `User Status Service`로 전송 (OpenFeign 사용)
- 로그인 시 `User Status`를 함께 조회하여 프론트에 통합 응답 제공

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot, Spring Security, JPA |
| 인증 | JWT |
| 통신 | OpenFeign |
| DB | MySQL |
| 테스트 | Postman |

---

## 🔗 API 문서
### [Postman API Document - User](https://documenter.getpostman.com/view/20776466/2sB2xEBU7G)

---
## 🧩 디렉토리 구조
```
└─📦callog_user
    ├─📂api
    │  ├─📂back
    │  └─📂open
    ├─📂common
    │  ├─📂dto
    │  ├─📂exception
    │  ├─📂handler
    │  └─📂web
    │      └─📂context
    ├─📂config
    │  └─📂jwt
    ├─📂domain
    │  ├─📂dto
    │  │  ├─📂token
    │  │  └─📂user
    │  └─📂entity
    ├─📂remote
    │  └─📂userstatus
    │      └─📂dto
    ├─📂repository
    ├─📂service
    └─📂validation
```
---
## ✍️ 개발자
| 이름 | 역할 | GitHub|
|------|------|--------|
| 임다희 | BackEnd |https://github.com/Imdahee |
| 황세현 | BackEnd |https://github.com/goozip2|


<!--
## 🚀 실행 방법

```bash
# 1. 프로젝트 빌드
./gradlew clean build

# 2. 애플리케이션 실행 (로컬)
java -jar build/libs/user-service.jar

# 또는 Docker 사용 시
docker-compose up -d
-->
