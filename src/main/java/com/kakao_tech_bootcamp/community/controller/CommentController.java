package com.kakao_tech_bootcamp.community.controller;

import com.kakao_tech_bootcamp.community.common.ApiResponse;
import com.kakao_tech_bootcamp.community.dto.CommentRequests;
import com.kakao_tech_bootcamp.community.dto.CommentResponses;
import com.kakao_tech_bootcamp.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 댓글 REST API 엔드포인트
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Map<String, CommentResponses.CommentResponse>>> createComment(
            @PathVariable Integer postId,
            @RequestParam Integer userId,
            @RequestBody CommentRequests.CreateCommentRequest req) {
        try {
            CommentResponses.CommentResponse comment = commentService.createComment(postId, userId, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("ok", Map.of("comment", comment)));
        } catch (Exception e) {
            log.error("댓글 작성 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("comment_creation_failed", null));
        }
    }

    // 게시글의 댓글 목록 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Map<String, CommentResponses.CommentListResponse>>> getCommentsByPostId(
            @PathVariable Integer postId) {
        try {
            CommentResponses.CommentListResponse comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("comments", comments)));
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("get_comments_failed", null));
        }
    }

    // 특정 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, CommentResponses.CommentResponse>>> getComment(
            @PathVariable Integer commentId) {
        try {
            CommentResponses.CommentResponse comment = commentService.getComment(commentId);
            return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("comment", comment)));
        } catch (Exception e) {
            log.error("댓글 조회 실패", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("comment_not_found", null));
        }
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, CommentResponses.CommentResponse>>> updateComment(
            @PathVariable Integer commentId,
            @RequestBody CommentRequests.UpdateCommentRequest req) {
        try {
            CommentResponses.CommentResponse comment = commentService.updateComment(commentId, req);
            return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("comment", comment)));
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("comment_update_failed", null));
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Integer commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(new ApiResponse<>("ok", null));
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("comment_delete_failed", null));
        }
    }

    // 댓글 통계 조회
    @GetMapping("/posts/{postId}/stats")
    public ResponseEntity<ApiResponse<CommentResponses.CommentStatsResponse>> getCommentStats(
            @PathVariable Integer postId) {
        try {
            CommentResponses.CommentStatsResponse stats = commentService.getCommentStats(postId);
            return ResponseEntity.ok(new ApiResponse<>("ok", stats));
        } catch (Exception e) {
            log.error("댓글 통계 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("get_comment_stats_failed", null));
        }
    }

    // 사용자의 댓글 목록 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, List<CommentResponses.CommentSummaryResponse>>>> getUserComments(
            @PathVariable Integer userId) {
        try {
            List<CommentResponses.CommentSummaryResponse> comments = commentService.getUserComments(userId);
            return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("comments", comments)));
        } catch (Exception e) {
            log.error("사용자 댓글 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("get_user_comments_failed", null));
        }
    }
}