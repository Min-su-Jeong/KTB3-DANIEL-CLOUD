package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.PostLike;
import com.kakao_tech_bootcamp.community.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// 게시글 좋아요 데이터 접근: 데이터베이스 조회/저장 담당
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    boolean existsByIdUserIdAndIdPostId(@Param("userId") Integer userId, @Param("postId") Integer postId);

    // 특정 게시글의 좋아요 개수 조회
    long countByIdPostId(@Param("postId") Integer postId);

    // 특정 사용자의 좋아요 조회
    Optional<PostLike> findByIdUserIdAndIdPostId(@Param("userId") Integer userId, @Param("postId") Integer postId);

    // 특정 게시글의 모든 좋아요 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM PostLike pl WHERE pl.id.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);
}