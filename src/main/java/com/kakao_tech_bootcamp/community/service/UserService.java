package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.User;
import com.kakao_tech_bootcamp.community.dto.UserRequests;
import com.kakao_tech_bootcamp.community.dto.UserResponses;
import com.kakao_tech_bootcamp.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 사용자 비즈니스 로직: 회원가입/로그인/조회/수정/비번변경/탈퇴 처리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponses.UserResponse signUp(UserRequests.SignUpRequest req) {
        validateSignUpRequest(req);
        
        String hash = hashPassword(req.password());
        User saved = userRepository.save(User.builder()
                .email(req.email())
                .password(hash)
                .nickname(req.nickname())
                .profileImageUrl(req.profileImageUrl())
                .build());

        return new UserResponses.UserResponse(saved.getUserId(), saved.getEmail(), saved.getNickname(), saved.getProfileImageUrl(), saved.getCreatedAt());
    }

    public UserResponses.LoginResponse login(UserRequests.LoginRequest req) {
        User user = userRepository.findActiveByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호를 확인해주세요."));
        if (!verifyPassword(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 확인해주세요.");
        }
        return new UserResponses.LoginResponse(user.getUserId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    public UserResponses.UserResponse getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponses.UserResponse(user.getUserId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl(), user.getCreatedAt());
    }

    @Transactional
    public UserResponses.UserResponse updateProfile(Integer userId, UserRequests.UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (req.nickname() != null && !req.nickname().equals(user.getNickname()) && userRepository.existsByNicknameAndDeletedAtIsNull(req.nickname())) {
            throw new IllegalArgumentException("중복된 닉네임 입니다.");
        }
        if (req.nickname() != null) user.setNickname(req.nickname());
        if (req.profileImageUrl() != null) user.setProfileImageUrl(req.profileImageUrl());

        User saved = userRepository.save(user);
        return new UserResponses.UserResponse(saved.getUserId(), saved.getEmail(), saved.getNickname(), saved.getProfileImageUrl(), saved.getCreatedAt());
    }

    @Transactional
    public void changePassword(Integer userId, UserRequests.ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (!verifyPassword(req.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        validateChangePasswordRequest(req);
        
        user.setPassword(hashPassword(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserResponses.DuplicateCheckResponse checkEmail(String email) {
        return new UserResponses.DuplicateCheckResponse(userRepository.existsByEmailAndDeletedAtIsNull(email));
    }

    public UserResponses.DuplicateCheckResponse checkNickname(String nickname) {
        return new UserResponses.DuplicateCheckResponse(userRepository.existsByNicknameAndDeletedAtIsNull(nickname));
    }

    // 회원가입 요청 검증
    private void validateSignUpRequest(UserRequests.SignUpRequest req) {
        if (req.email() == null || req.password() == null || req.confirmPassword() == null || req.nickname() == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 확인과 다릅니다.");
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new IllegalArgumentException("중복된 이메일 입니다.");
        }
        if (userRepository.existsByNicknameAndDeletedAtIsNull(req.nickname())) {
            throw new IllegalArgumentException("중복된 닉네임 입니다.");
        }
    }

    // 비밀번호 변경 요청 검증
    private void validateChangePasswordRequest(UserRequests.ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
    }

    // 비밀번호 해시 함수 (SHA256)
    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // salt + hash를 합쳐서 저장
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    // 비밀번호 검증 함수
    private boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // 해시 비교
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != combined[16 + i]) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
