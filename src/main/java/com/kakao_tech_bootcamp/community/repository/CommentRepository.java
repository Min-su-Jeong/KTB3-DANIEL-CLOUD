package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 댓글 데이터 접근: 댓글 CRUD 및 대댓글 처리
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 특정 게시글의 댓글 목록 조회 (계층 구조)
    @Query("SELECT c FROM Comment c WHERE c.post.postId = :postId ORDER BY c.parentId ASC NULLS FIRST, c.createdAt ASC")
    List<Comment> findByPostIdOrderByParentIdAscCreatedAtAsc(@Param("postId") Integer postId);

    // 특정 게시글의 댓글 개수 조회
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.postId = :postId")
    Long countByPostId(@Param("postId") Integer postId);

    // 특정 댓글의 대댓글 개수 조회
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentId = :parentId")
    Long countByParentId(@Param("parentId") Integer parentId);

    // 특정 게시글의 댓글 삭제
    void deleteByPostId(@Param("postId") Integer postId);

    // 특정 댓글과 대댓글들 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.commentId = :commentId OR c.parentId = :commentId")
    void deleteCommentAndReplies(@Param("commentId") Integer commentId);

    // 특정 사용자의 댓글 목록 조회
    @Query("SELECT c FROM Comment c WHERE c.user.userId = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
}
