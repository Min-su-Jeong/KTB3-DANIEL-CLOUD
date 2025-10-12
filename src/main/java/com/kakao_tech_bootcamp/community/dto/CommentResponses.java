package com.kakao_tech_bootcamp.community.dto;

import java.time.LocalDateTime;
import java.util.List;

// 댓글 응답 DTO: 서버에서 클라이언트로 전송하는 데이터
public class CommentResponses {

    // 댓글 정보 응답 DTO
    public record CommentResponse(
            Integer commentId,
            Integer postId,
            Integer userId,
            Integer parentId,
            String content,
            Integer depth,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<CommentResponse> replies // 대댓글 목록
    ) {}

    // 댓글 목록 응답 DTO (계층 구조)
    public record CommentListResponse(
            List<CommentResponse> comments,
            Long totalCount
    ) {}

    // 댓글 요약 응답 DTO (게시글 목록용)
    public record CommentSummaryResponse(
            Integer commentId,
            Integer userId,
            String content,
            Integer depth,
            LocalDateTime createdAt
    ) {}

    // 댓글 통계 응답 DTO
    public record CommentStatsResponse(
            Integer postId,
            Long totalCommentCount,
            Long replyCount
    ) {}
}