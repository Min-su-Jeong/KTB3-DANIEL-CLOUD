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

    // 특정 사용자의 이미지 목록 조회 (soft delete 고려)
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Image> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 이미지 조회 (soft delete 고려)
    @Query("SELECT i FROM Image i WHERE i.imageId = :imageId AND i.deletedAt IS NULL")
    Optional<Image> findActiveById(@Param("imageId") Long imageId);

    // 파일 URL로 이미지 조회 (중복 체크용)
    @Query("SELECT i FROM Image i WHERE i.fileUrl = :fileUrl AND i.deletedAt IS NULL")
    Optional<Image> findByFileUrlAndDeletedAtIsNull(@Param("fileUrl") String fileUrl);

    // 이미지 soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Image i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.imageId = :imageId AND i.deletedAt IS NULL")
    void softDeleteById(@Param("imageId") Long imageId);

    // 특정 사용자의 모든 이미지 soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Image i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.userId = :userId AND i.deletedAt IS NULL")
    void softDeleteByUserId(@Param("userId") Long userId);

    // 사용하지 않는 이미지 조회 (참조되지 않는 이미지들)
    @Query("SELECT i FROM Image i WHERE i.deletedAt IS NULL AND i.imageId NOT IN " +
           "(SELECT pi.image.imageId FROM PostImage pi WHERE pi.deletedAt IS NULL) " +
           "AND i.imageId NOT IN (SELECT u.profileImage.imageId FROM User u WHERE u.deletedAt IS NULL AND u.profileImage IS NOT NULL)")
    List<Image> findUnusedImages();
}
