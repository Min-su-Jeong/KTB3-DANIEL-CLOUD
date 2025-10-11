package com.kakao_tech_bootcamp.community.dto;

// 게시글 요청 DTO: 클라이언트에서 서버로 전송하는 데이터
public class PostRequests {

    // 게시글 작성 요청 DTO
    public record CreatePostRequest(
            Integer userId,
            String title,
            String content
    ) {}

    // 게시글 수정 요청 DTO
    public record UpdatePostRequest(
            String title,
            String content
    ) {}
}
