package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 사용자 데이터 접근: 데이터베이스 조회/저장 담당
public interface UserRepository extends JpaRepository<User, Integer> {

    // 활성 사용자 중복 체크 (soft delete 고려)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsActiveByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    boolean existsActiveByNickname(@Param("nickname") String nickname);

    // 활성 사용자 조회 (soft delete 고려)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Integer id);
}
