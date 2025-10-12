package com.kakao_tech_bootcamp.community.dto;

import java.time.LocalDateTime;

// 사용자 응답 DTO: 서버에서 클라이언트로 전송하는 데이터
public class UserResponses {

    // 사용자 정보 응답 DTO
    public record UserResponse(
            Integer userId,
            String email,
            String nickname,
            String profileImageUrl,
            LocalDateTime createdAt
    ) {}

    // 로그인 응답 DTO
    public record LoginResponse(
            Integer userId,
            String email,
            String nickname,
            String profileImageUrl
    ) {}

    // 중복 체크 응답 DTO
    public record DuplicateCheckResponse(
            boolean isDuplicate
    ) {}
}
