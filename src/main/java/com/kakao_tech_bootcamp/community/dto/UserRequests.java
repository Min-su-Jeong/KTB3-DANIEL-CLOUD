package com.kakao_tech_bootcamp.community.dto;

// 사용자 요청 DTO: 클라이언트에서 서버로 전송하는 데이터
public class UserRequests {

    // 회원가입 요청 DTO
    public record SignUpRequest(
            String email,
            String password,
            String confirmPassword,
            String nickname,
            String profileImageUrl
    ) {}

    // 로그인 요청 DTO
    public record LoginRequest(
            String email,
            String password
    ) {}

    // 프로필 수정 요청 DTO
    public record UpdateProfileRequest(
            String nickname,
            String profileImageUrl
    ) {}

    // 비밀번호 변경 요청 DTO
    public record ChangePasswordRequest(
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {}

    // 이메일 체크 요청 DTO
    public record EmailCheckRequest(
            String email
    ) {}

    // 닉네임 체크 요청 DTO
    public record NicknameCheckRequest(
            String nickname
    ) {}
}
