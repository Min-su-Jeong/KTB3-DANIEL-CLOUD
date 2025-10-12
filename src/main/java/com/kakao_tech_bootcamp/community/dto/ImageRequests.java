package com.kakao_tech_bootcamp.community.dto;

// 이미지 요청 DTO: 클라이언트에서 서버로 전송하는 데이터
public class ImageRequests {

    // 이미지 순서 변경 요청 DTO
    public record UpdateImageOrderRequest(
            Integer newOrder
    ) {}

    // 이미지 업로드 요청 DTO
    public record ImageUploadRequest(
            Long userId,
            String description
    ) {}

    // 게시글 이미지 추가 요청 DTO
    public record PostImageAddRequest(
            Integer postId,
            String description
    ) {}
}
