package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

// 게시글 이미지 데이터 접근: 데이터베이스 조회/저장 담당
public interface PostImageRepository extends JpaRepository<PostImage, com.kakao_tech_bootcamp.community.entity.PostImageId> {

    // 특정 게시글의 이미지 목록 조회 (순서대로 정렬)
    @Query("SELECT pi FROM PostImage pi WHERE pi.post.postId = :postId ORDER BY pi.imageOrder ASC, pi.createdAt ASC")
    java.util.List<PostImage> findByPostIdOrderByImageOrderAsc(@Param("postId") Integer postId);

    // 특정 이미지 ID로 PostImage 조회
    java.util.Optional<PostImage> findByImageId(@Param("imageId") Integer imageId);

    // 특정 게시글의 다음 이미지 순서 조회
    @Query("SELECT COALESCE(MAX(pi.imageOrder), 0) + 1 FROM PostImage pi WHERE pi.post.postId = :postId")
    Integer findNextImageOrder(@Param("postId") Integer postId);

    // 특정 게시글의 모든 이미지 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM PostImage pi WHERE pi.post.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);
}