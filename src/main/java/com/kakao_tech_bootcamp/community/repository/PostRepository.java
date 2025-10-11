package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


// 게시글 데이터 접근: 데이터베이스 조회/저장 담당
public interface PostRepository extends JpaRepository<Post, Integer> {

    // 인피니티 스크롤용 게시글 조회 (첫 페이지)
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findFirstPagePosts(org.springframework.data.domain.Pageable pageable);

    // 인피니티 스크롤용 게시글 조회 (lastPostId 이후의 게시글들)
    @Query("SELECT p FROM Post p WHERE p.postId < :lastPostId ORDER BY p.createdAt DESC")
    List<Post> findPostsAfterId(@Param("lastPostId") Integer lastPostId, org.springframework.data.domain.Pageable pageable);
}