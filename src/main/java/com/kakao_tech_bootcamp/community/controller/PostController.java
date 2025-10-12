package com.kakao_tech_bootcamp.community.controller;

import com.kakao_tech_bootcamp.community.common.ApiResponse;
import com.kakao_tech_bootcamp.community.dto.PostRequests;
import com.kakao_tech_bootcamp.community.dto.PostResponses;
import com.kakao_tech_bootcamp.community.service.PostService;
import com.kakao_tech_bootcamp.community.service.PostLikeService;
import com.kakao_tech_bootcamp.community.service.PostStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 게시글 REST API 엔드포인트
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostStatService postStatService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(@RequestBody PostRequests.CreatePostRequest req) {
        postService.createPost(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("ok", null));
    }

    // 게시글 목록 조회 (인피니티 스크롤)
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<PostResponses.PostSummaryResponse>>>> getPosts(
            @RequestParam(value = "lastPostId", required = false) Integer lastPostId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        List<PostResponses.PostSummaryResponse> posts = postService.getPosts(lastPostId, limit);
        return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("posts", posts)));
    }

    // 특정 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<Map<String, PostResponses.PostResponse>>> getPost(@PathVariable Integer postId) {
        PostResponses.PostResponse post = postService.getPost(postId);
        return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("post", post)));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(@PathVariable Integer postId, @RequestBody PostRequests.UpdatePostRequest req) {
        postService.updatePost(postId, req);
        return ResponseEntity.ok(new ApiResponse<>("ok", null));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Integer postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(new ApiResponse<>("ok", null));
    }

    // 게시글 좋아요 추가
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> addPostLike(
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        postLikeService.addPostLike(userId, postId);
        int likeCount = postLikeService.getPostLikeCount(postId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("ok", Map.of("likeCount", likeCount)));
    }

    // 게시글 좋아요 삭제
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> removePostLike(
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        postLikeService.removePostLike(userId, postId);
        int likeCount = postLikeService.getPostLikeCount(postId);
        return ResponseEntity.ok(new ApiResponse<>("ok", Map.of("likeCount", likeCount)));
    }

    // 게시글 좋아요 상태 조회
    @GetMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PostResponses.LikeStatusResponse>> getPostLikeStatus(
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        PostResponses.LikeStatusResponse response = postLikeService.getPostLikeStatus(userId, postId);
        return ResponseEntity.ok(new ApiResponse<>("ok", response));
    }

    // 게시글 통계 조회
    @GetMapping("/{postId}/stats")
    public ResponseEntity<ApiResponse<PostResponses.PostStatResponse>> getPostStats(@PathVariable Integer postId) {
        PostResponses.PostStatResponse stats = postStatService.getPostStats(postId);
        return ResponseEntity.ok(new ApiResponse<>("ok", stats));
    }
}