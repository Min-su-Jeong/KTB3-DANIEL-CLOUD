package com.kakao_tech_bootcamp.community.dto;

// 댓글 요청 DTO: 클라이언트에서 서버로 전송하는 데이터
public class CommentRequests {

    // 댓글 작성 요청 DTO
    public record CreateCommentRequest(
            Integer parentId,
            String content
    ) {}

    // 댓글 수정 요청 DTO
    public record UpdateCommentRequest(
            String content
    ) {}
}