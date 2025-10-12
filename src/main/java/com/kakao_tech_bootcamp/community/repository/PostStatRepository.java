package com.kakao_tech_bootcamp.community.repository;

import com.kakao_tech_bootcamp.community.entity.PostStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostStatRepository extends JpaRepository<PostStat, Integer> {

    // 게시글 통계 조회
    @Query("SELECT ps FROM PostStat ps WHERE ps.postId = :postId")
    PostStat findByPostId(@Param("postId") Integer postId);

    // 좋아요 개수 증가
    @Modifying
    @Query("UPDATE PostStat ps SET ps.likeCount = ps.likeCount + 1 WHERE ps.postId = :postId")
    void incrementLikeCount(@Param("postId") Integer postId);

    // 좋아요 개수 감소
    @Modifying
    @Query("UPDATE PostStat ps SET ps.likeCount = ps.likeCount - 1 WHERE ps.postId = :postId")
    void decrementLikeCount(@Param("postId") Integer postId);

    // 조회수 증가
    @Modifying
    @Query("UPDATE PostStat ps SET ps.viewCount = ps.viewCount + 1 WHERE ps.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    // 댓글 개수 증가
    @Modifying
    @Query("UPDATE PostStat ps SET ps.commentCount = ps.commentCount + 1 WHERE ps.postId = :postId")
    void incrementCommentCount(@Param("postId") Integer postId);

    // 댓글 개수 감소
    @Modifying
    @Query("UPDATE PostStat ps SET ps.commentCount = ps.commentCount - 1 WHERE ps.postId = :postId")
    void decrementCommentCount(@Param("postId") Integer postId);

    // 게시글 삭제 시 통계도 삭제
    @Modifying
    @Query("DELETE FROM PostStat ps WHERE ps.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);
}
