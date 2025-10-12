package com.kakao_tech_bootcamp.community.controller;

import com.kakao_tech_bootcamp.community.common.ApiResponse;
import com.kakao_tech_bootcamp.community.dto.UserRequests;
import com.kakao_tech_bootcamp.community.dto.UserResponses;
import com.kakao_tech_bootcamp.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 사용자 REST API 엔드포인트
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveUser(@RequestBody UserRequests.SignUpRequest req) {
        userService.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("register_success", null));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponses.LoginResponse>> login(@RequestBody UserRequests.LoginRequest req) {
        UserResponses.LoginResponse response = userService.login(req);
        return ResponseEntity.ok(new ApiResponse<>("login_success", response));
    }

    // 이메일 중복 체크
    @PostMapping("/availability/email")
    public ResponseEntity<ApiResponse<UserResponses.DuplicateCheckResponse>> isAvailableEmail(@RequestBody UserRequests.EmailCheckRequest req) {
        UserResponses.DuplicateCheckResponse response = userService.checkEmail(req.email());
        return ResponseEntity.ok(new ApiResponse<>("ok", response));
    }

    // 닉네임 중복 체크
    @PostMapping("/availability/nickname")
    public ResponseEntity<ApiResponse<UserResponses.DuplicateCheckResponse>> isAvailableNickname(@RequestBody UserRequests.NicknameCheckRequest req) {
        UserResponses.DuplicateCheckResponse response = userService.checkNickname(req.nickname());
        return ResponseEntity.ok(new ApiResponse<>("ok", response));
    }
    
    // 사용자 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, UserResponses.UserResponse>>> findUser(@PathVariable Integer userId) {
        UserResponses.UserResponse user = userService.getUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("user", user)));
    }

    // 프로필 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> modifyUser(@PathVariable Integer userId, @RequestBody UserRequests.UpdateProfileRequest req) {
        userService.updateProfile(userId, req);
        return ResponseEntity.ok(new ApiResponse<>("update_success", null));
    }

    // 비밀번호 변경
    @PatchMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Integer userId, @RequestBody UserRequests.ChangePasswordRequest req) {
        userService.changePassword(userId, req);
        return ResponseEntity.ok(new ApiResponse<>("password_change_success", null));
    }

    // 회원 탈퇴 (소프트 삭제)
    @PatchMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse<Void>> removeUser(@PathVariable Integer userId) {
        userService.delete(userId);
        return ResponseEntity.ok(new ApiResponse<>("delete_success", null));
    }
}