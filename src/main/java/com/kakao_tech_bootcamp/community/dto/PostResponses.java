package com.kakao_tech_bootcamp.community.dto;

import java.time.LocalDateTime;

// 게시글 응답 DTO: 서버에서 클라이언트로 전송하는 데이터
public class PostResponses {

    // 게시글 정보 응답 DTO (게시글 상세 조회 용도)
    public record PostResponse(
            Integer postId,
            Integer userId,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            java.util.List<com.kakao_tech_bootcamp.community.dto.ImageResponses.PostImageResponse> images,
            String authorNickname,
            String authorProfileImageUrl,
            Integer likeCount,
            Integer commentCount,
            Integer viewCount
    ) {}

    // 게시글 목록 응답 DTO (게시글 목록 조회 용도)
    public record PostSummaryResponse(
            Integer postId,
            Integer userId,
            String title,
            LocalDateTime createdAt,
            Integer likeCount,
            Integer commentCount,
            Integer viewCount,
            String authorNickname,
            String authorProfileImageUrl
    ) {}

    // 좋아요 상태 응답 DTO
    public record LikeStatusResponse(
            boolean isLiked,
            Integer likeCount
    ) {}

    // 게시글 통계 응답 DTO
    public record PostStatResponse(
            Integer postId,
            Integer likeCount,
            Integer viewCount,
            Integer commentCount
    ) {}
}