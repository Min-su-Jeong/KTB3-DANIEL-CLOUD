package com.kakao_tech_bootcamp.community.controller;

import com.kakao_tech_bootcamp.community.common.ApiResponse;
import com.kakao_tech_bootcamp.community.entity.Image;
import com.kakao_tech_bootcamp.community.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// 공용 이미지 REST API 엔드포인트
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    // 이미지 업로드
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId) {
        try {
            Image image = imageService.uploadImage(userId, file);
            
            Map<String, Object> data = Map.of(
                "imageId", image.getImageId(),
                "fileUrl", image.getFileUrl(),
                "thumbnailUrl", image.getThumbnailUrl()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("upload_success", data));
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("upload_failed", null));
        }
    }

    // 이미지 조회
    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImage(@PathVariable Long imageId) {
        try {
            Image image = imageService.getImage(imageId);
            
            Map<String, Object> data = Map.of(
                "imageId", image.getImageId(),
                "fileUrl", image.getFileUrl(),
                "thumbnailUrl", image.getThumbnailUrl(),
                "userId", image.getUserId()
            );
            
            return ResponseEntity.ok(new ApiResponse<>("success", data));
        } catch (Exception e) {
            log.error("이미지 조회 실패", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("image_not_found", null));
        }
    }

    // 사용자 이미지 목록 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> getUserImages(@PathVariable Integer userId) {
        try {
            List<Image> images = imageService.getUserImages(userId);
            
            List<Map<String, Object>> imageList = images.stream()
                .map(image -> {
                    Map<String, Object> imageMap = new java.util.HashMap<>();
                    imageMap.put("imageId", image.getImageId());
                    imageMap.put("fileUrl", image.getFileUrl());
                    imageMap.put("thumbnailUrl", image.getThumbnailUrl());
                    return imageMap;
                })
                .toList();
            
            return ResponseEntity.ok(new ApiResponse<>("success", Map.of("images", imageList)));
        } catch (Exception e) {
            log.error("사용자 이미지 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("get_images_failed", null));
        }
    }

    // 이미지 삭제
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(new ApiResponse<>("delete_success", null));
        } catch (Exception e) {
            log.error("이미지 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("delete_failed", null));
        }
    }

    // 게시글 이미지 추가
    @PostMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addImagesToPost(
            @PathVariable Integer postId,
            @RequestParam List<MultipartFile> files) {
        try {
            List<String> imageUrls = imageService.addImagesToPost(postId, files);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("upload_success", Map.of("imageUrls", imageUrls)));
        } catch (Exception e) {
            log.error("게시글 이미지 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("upload_failed", null));
        }
    }

    // 게시글 이미지 목록 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> getPostImages(
            @PathVariable Integer postId) {
        try {
            List<com.kakao_tech_bootcamp.community.dto.ImageResponses.PostImageResponse> images = 
                imageService.getPostImages(postId);
            
            List<Map<String, Object>> imageList = images.stream()
                .map(image -> {
                    Map<String, Object> imageMap = new java.util.HashMap<>();
                    imageMap.put("imageId", image.imageId());
                    imageMap.put("postId", image.postId());
                    imageMap.put("fileUrl", image.imageUrl());
                    imageMap.put("imageOrder", image.imageOrder());
                    imageMap.put("createdAt", image.createdAt());
                    return imageMap;
                })
                .toList();
            
            return ResponseEntity.ok(new ApiResponse<>("success", Map.of("images", imageList)));
        } catch (Exception e) {
            log.error("게시글 이미지 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("get_images_failed", null));
        }
    }

    // 게시글 이미지 삭제
    @DeleteMapping("/posts/{imageId}")
    public ResponseEntity<ApiResponse<Void>> removeImageFromPost(@PathVariable Integer imageId) {
        try {
            imageService.removeImageFromPost(imageId);
            return ResponseEntity.ok(new ApiResponse<>("delete_success", null));
        } catch (Exception e) {
            log.error("게시글 이미지 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("delete_failed", null));
        }
    }

    // 게시글 이미지 순서 변경
    @PutMapping("/posts/{imageId}/order")
    public ResponseEntity<ApiResponse<Void>> updatePostImageOrder(
            @PathVariable Integer imageId,
            @RequestBody com.kakao_tech_bootcamp.community.dto.ImageRequests.UpdateImageOrderRequest req) {
        try {
            imageService.updatePostImageOrder(imageId, req);
            return ResponseEntity.ok(new ApiResponse<>("update_success", null));
        } catch (Exception e) {
            log.error("게시글 이미지 순서 변경 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("update_failed", null));
        }
    }

    // 사용하지 않는 이미지 정리
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Map<String, String>>> cleanupUnusedImages() {
        try {
            imageService.cleanupUnusedImages();
            return ResponseEntity.ok(new ApiResponse<>("cleanup_success", 
                Map.of("message", "사용하지 않는 이미지 정리가 완료되었습니다.")));
        } catch (Exception e) {
            log.error("이미지 정리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("cleanup_failed", null));
        }
    }
}