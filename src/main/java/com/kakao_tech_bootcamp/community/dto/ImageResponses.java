package com.kakao_tech_bootcamp.community.dto;

import java.time.LocalDateTime;

// 이미지 응답 DTO: 서버에서 클라이언트로 전송하는 데이터
public class ImageResponses {

    // 공용 이미지 응답 DTO
    public record ImageResponse(
            Long imageId,
            String fileUrl,
            String thumbnailUrl,
            String fileName,
            Long fileSize,
            String fileType,
            Integer width,
            Integer height,
            Long userId,
            LocalDateTime createdAt
    ) {}

    // 게시글 이미지 응답 DTO
    public record PostImageResponse(
            Integer imageId,
            Integer postId,
            String imageUrl,
            Integer imageOrder,
            LocalDateTime createdAt
    ) {}

    // 사용자 이미지 목록 응답 DTO
    public record UserImageListResponse(
            Long userId,
            java.util.List<ImageResponse> images
    ) {}

    // 게시글 이미지 목록 응답 DTO
    public record PostImageListResponse(
            Integer postId,
            java.util.List<PostImageResponse> images
    ) {}

    // 게시글 썸네일 이미지 응답 DTO (목록용)
    public record PostThumbnailResponse(
            Integer imageId,
            String imageUrl,
            Integer imageOrder
    ) {}
}
