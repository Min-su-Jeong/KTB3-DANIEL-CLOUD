# 커뮤니티 백엔드 API

Spring Boot를 활용한 커뮤니티 백엔드 API 구현하기

## 주요 기능

### 사용자 관리
- **회원가입/로그인**: 이메일 기반 사용자 인증
- **프로필 관리**: 닉네임, 프로필 이미지 수정
- **중복 체크**: 이메일, 닉네임 중복 확인
- **비밀번호 변경**: 사용자 비밀번호 업데이트
- **회원 탈퇴**: 소프트 삭제 방식

### 게시글 관리
- **게시글 CRUD**: 작성, 조회, 수정, 삭제
- **무한 스크롤**: 페이지네이션을 통한 게시글 목록 조회
- **게시글 통계**: 조회수, 좋아요 수 등 통계 정보
- **좋아요 기능**: 게시글 좋아요/취소

### 댓글 시스템
- **댓글 CRUD**: 댓글 작성, 조회, 수정, 삭제
- **대댓글 지원**: 계층형 댓글 구조 (depth 기반)
- **댓글 통계**: 댓글 수 통계
- **사용자별 댓글**: 특정 사용자의 댓글 목록 조회

### 이미지 관리
- **이미지 업로드**: 게시글 첨부 이미지 업로드
- **프로필 이미지**: 사용자 프로필 이미지 관리
- **파일 크기 제한**: 개별 파일 10MB, 전체 요청 100MB

## 기술 스택

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL** (데이터베이스)
- **Gradle** (빌드 도구)
- **Lombok** (코드 간소화)
- **P6Spy** (SQL 로깅)

## 프로젝트 구조

```
src/main/java/com/kakao_tech_bootcamp/community/
├── common/           # 공통 응답 클래스
├── controller/       # REST API 컨트롤러
│   ├── UserController.java
│   ├── PostController.java
│   ├── CommentController.java
│   └── ImageController.java
├── dto/             # 요청/응답 DTO
│   ├── UserRequests.java
│   ├── UserResponses.java
│   ├── PostRequests.java
│   ├── PostResponses.java
│   ├── CommentRequests.java
│   └── CommentResponses.java
├── entity/          # JPA 엔티티
│   ├── User.java
│   ├── Post.java
│   ├── Comment.java
│   ├── Image.java
│   ├── PostImage.java
│   └── PostStat.java
├── repository/      # 데이터 접근 계층
├── service/         # 비즈니스 로직
└── exception/       # 예외 처리
```

## 데이터베이스 설계

### 테이블
- `user`: 사용자 정보
- `post`: 게시글
- `comment`: 댓글
- `image`: 이미지 파일 정보
- `post_image`: 게시글-이미지 연결
- `post_stat`: 게시글 통계

## API 엔드포인트

### 사용자 API
- `POST /users` - 회원가입
- `POST /users/login` - 로그인
- `GET /users/{userId}` - 사용자 조회
- `PATCH /users/{userId}` - 프로필 수정
- `PATCH /users/{userId}/password` - 비밀번호 변경
- `PATCH /users/{userId}/delete` - 회원 탈퇴 (소프트 삭제)

### 게시글 API
- `POST /posts` - 게시글 작성
- `GET /posts` - 게시글 목록 (무한 스크롤)
- `GET /posts/{postId}` - 게시글 상세 조회
- `PATCH /posts/{postId}` - 게시글 수정
- `DELETE /posts/{postId}` - 게시글 삭제
- `POST /posts/{postId}/likes` - 게시글 좋아요 추가
- `DELETE /posts/{postId}/likes` - 게시글 좋아요 삭제
- `GET /posts/{postId}/likes` - 게시글 좋아요 상태 조회
- `GET /posts/{postId}/stats` - 게시글 통계 조회

### 댓글 API
- `POST /comments/posts/{postId}` - 댓글 작성
- `GET /comments/posts/{postId}` - 게시글 댓글 목록
- `GET /comments/{commentId}` - 댓글 상세 조회
- `PUT /comments/{commentId}` - 댓글 수정
- `DELETE /comments/{commentId}` - 댓글 삭제
- `GET /comments/posts/{postId}/stats` - 댓글 통계 조회
- `GET /comments/users/{userId}` - 사용자 댓글 목록 조회

### 이미지 API
- `POST /images` - 이미지 업로드
- `GET /images/{imageId}` - 이미지 조회
- `GET /images/users/{userId}` - 사용자 이미지 목록 조회
- `DELETE /images/{imageId}` - 이미지 삭제
- `POST /images/posts/{postId}` - 게시글 이미지 추가
- `GET /images/posts/{postId}` - 게시글 이미지 목록 조회
- `DELETE /images/posts/{imageId}` - 게시글 이미지 삭제
- `PUT /images/posts/{imageId}/order` - 게시글 이미지 순서 변경
- `POST /images/cleanup` - 사용하지 않는 이미지 정리

## 향후 개선 계획

- **인증/인가**: JWT 토큰 기반 인증 시스템 도입
- **파일 저장소**: AWS S3 연동으로 이미지 저장 방식 개선
- **캐싱**: Redis를 활용한 성능 최적화
