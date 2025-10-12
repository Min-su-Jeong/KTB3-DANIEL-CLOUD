package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 사용자 데이터 접근: 데이터베이스 조회/저장 담당
public interface UserRepository extends JpaRepository<User, Integer> {

    // 사용자 이메일 & 닉네임 중복 체크 (soft delete 고려) - Spring Data JPA 자동 쿼리 생성
    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    // 사용자 이메일 & 닉네임 조회 (soft delete 고려)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.userId = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Integer id);
}