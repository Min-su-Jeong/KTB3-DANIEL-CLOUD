package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// 공용 이미지 데이터 접근: 이미지 중앙 관리
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 특정 사용자의 이미지 목록 조회
    List<Image> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);

    // 파일 URL로 이미지 조회 (중복 체크용)
    Optional<Image> findByFileUrl(@Param("fileUrl") String fileUrl);

    // 특정 사용자의 모든 이미지 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    // 사용하지 않는 이미지 조회 (참조되지 않는 이미지들)
    @Query("SELECT i FROM Image i WHERE i.imageId NOT IN " +
           "(SELECT u.profileImage.imageId FROM User u WHERE u.profileImage IS NOT NULL)")
    List<Image> findUnusedImages();
}
