package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.Image;
import com.kakao_tech_bootcamp.community.entity.PostImage;
import com.kakao_tech_bootcamp.community.repository.ImageRepository;
import com.kakao_tech_bootcamp.community.repository.PostImageRepository;
import com.kakao_tech_bootcamp.community.repository.PostRepository;
import com.kakao_tech_bootcamp.community.dto.ImageRequests;
import com.kakao_tech_bootcamp.community.dto.ImageResponses;
import com.kakao_tech_bootcamp.community.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

// 공용 이미지 비즈니스 로직: 이미지 중앙 관리 및 중복 방지
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final FileUploadConfig fileUploadConfig;

    // 이미지 업로드 (중복 체크 포함)
    @Transactional
    public Image uploadImage(Long userId, MultipartFile file) {
        // 파일 검증
        validateImageFile(file);
        
        // 파일 업로드
        String fileUrl = uploadImageFile(file);
        
        // 중복 체크
        Image existingImage = imageRepository.findByFileUrlAndDeletedAtIsNull(fileUrl).orElse(null);
        if (existingImage != null) {
            log.info("중복 이미지 발견, 기존 이미지 반환: imageId={}, fileUrl={}", existingImage.getImageId(), fileUrl);
            return existingImage;
        }
        
        // 이미지 메타데이터 추출
        ImageMetadata metadata = extractImageMetadata(file);
        
        // 썸네일 생성
        String thumbnailUrl = generateThumbnail(fileUrl, metadata);
        
        // Image 엔티티 저장
        Image image = Image.builder()
            .fileUrl(fileUrl)
            .thumbnailUrl(thumbnailUrl)
            .userId(userId)
            .build();
        
        Image savedImage = imageRepository.save(image);
        log.info("이미지 업로드 완료: imageId={}, fileUrl={}", savedImage.getImageId(), fileUrl);
        
        return savedImage;
    }

    // 이미지 조회
    public Image getImage(Long imageId) {
        return imageRepository.findActiveById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
    }

    // 사용자 이미지 목록 조회
    public List<Image> getUserImages(Long userId) {
        return imageRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
    }

    // 이미지 삭제 (soft delete)
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findActiveById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        
        // 파일 삭제
        deleteImageFile(image.getFileUrl());
        if (image.getThumbnailUrl() != null) {
            deleteImageFile(image.getThumbnailUrl());
        }
        
        // soft delete
        imageRepository.softDeleteById(imageId);
        log.info("이미지 삭제 완료: imageId={}", imageId);
    }

    // 사용하지 않는 이미지 정리
    @Transactional
    public void cleanupUnusedImages() {
        List<Image> unusedImages = imageRepository.findUnusedImages();
        
        for (Image image : unusedImages) {
            // 파일 삭제
            deleteImageFile(image.getFileUrl());
            if (image.getThumbnailUrl() != null) {
                deleteImageFile(image.getThumbnailUrl());
            }
            
            // soft delete
            imageRepository.softDeleteById(image.getImageId());
        }
        
        log.info("사용하지 않는 이미지 정리 완료: {}개", unusedImages.size());
    }

    // ========== 게시글 이미지 관련 기능 ==========

    // 게시글 이미지 추가
    @Transactional
    public List<String> addImagesToPost(Integer postId, List<MultipartFile> files) {
        // 게시글 존재 여부 검증
        validatePostExists(postId);
        
        // 파일 개수 검증
        if (files.size() > 10) {
            throw new IllegalArgumentException("한 번에 최대 10개의 이미지만 업로드할 수 있습니다.");
        }
        
        List<String> imageUrls = new java.util.ArrayList<>();
        
        for (MultipartFile file : files) {
            // ImageService를 통해 이미지 업로드 (중복 체크 포함)
            Image image = uploadImage((long) postId, file);
            
            // 다음 순서 계산
            Integer nextOrder = postImageRepository.findNextImageOrder(postId);
            
            // PostImage 엔티티 생성 및 저장
            PostImage postImage = new PostImage(
                postId,
                image.getImageId(),
                nextOrder
            );
            
            postImageRepository.save(postImage);
            imageUrls.add(image.getFileUrl());
        }
        
        log.info("게시글 이미지 추가 완료: postId={}, count={}", postId, files.size());
        return imageUrls;
    }

    // 게시글 이미지 목록 조회
    public List<ImageResponses.PostImageResponse> getPostImages(Integer postId) {
        // 게시글 존재 여부 검증
        validatePostExists(postId);
        
        List<PostImage> postImages = postImageRepository.findByPostIdAndDeletedAtIsNullOrderByDisplayOrderAsc(postId);
        
        return postImages.stream()
                .map(postImage -> new ImageResponses.PostImageResponse(
                    postImage.getId().getImageId(),
                    postImage.getId().getPostId(),
                    getImageUrl(postImage.getId().getImageId()), // Image 엔티티에서 URL 조회
                    postImage.getDisplayOrder(),
                    postImage.getCreatedAt()
                ))
                .toList();
    }

    // Image ID로 URL 조회
    private String getImageUrl(Integer imageId) {
        Image image = imageRepository.findById((long) imageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        return image.getFileUrl();
    }

    // 게시글 이미지 삭제
    @Transactional
    public void removeImageFromPost(Integer imageId) {
        // PostImage 조회
        PostImage postImage = postImageRepository.findByImageIdAndDeletedAtIsNull(imageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        
        // PostImage soft delete
        postImage.markAsDeleted();
        postImageRepository.save(postImage);
        
        log.info("게시글 이미지 삭제 완료: imageId={}", imageId);
    }

    // 게시글 이미지 순서 변경
    @Transactional
    public void updatePostImageOrder(Integer imageId, ImageRequests.UpdateImageOrderRequest req) {
        // PostImage 조회
        PostImage postImage = postImageRepository.findByImageIdAndDeletedAtIsNull(imageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        
        // 순서 검증
        if (req.newOrder() < 1) {
            throw new IllegalArgumentException("이미지 순서는 1 이상이어야 합니다.");
        }
        
        // 순서 업데이트
        postImage.updateOrder(req.newOrder());
        postImageRepository.save(postImage);
        
        log.info("게시글 이미지 순서 변경 완료: imageId={}, newOrder={}", imageId, req.newOrder());
    }

    // 게시글 존재 여부 검증
    private void validatePostExists(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("게시글 ID가 필요합니다.");
        }
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
    }

    // ========== 유틸리티 메서드 ==========
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        
        if (file.getSize() > fileUploadConfig.getMaxSizeInBytes()) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다");
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!fileUploadConfig.getAllowedExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }
    }
    
    private String uploadImageFile(MultipartFile file) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String uploadDir = fileUploadConfig.getUploadDir() + "/images";
            createUploadDirectory(uploadDir);
            
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return "/images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다", e);
        }
    }
    
    private String generateThumbnail(String fileUrl, ImageMetadata metadata) {
        try {
            // 썸네일 생성 로직 (200x200)
            String thumbnailFileName = "thumb_" + getFileNameFromUrl(fileUrl);
            
            // 썸네일 생성 (실제 구현에서는 이미지 리사이징 로직 필요)
            // 여기서는 단순히 원본 URL을 반환
            return "/images/thumbnails/" + thumbnailFileName;
        } catch (Exception e) {
            log.warn("썸네일 생성 실패: {}", e.getMessage());
            return null;
        }
    }
    
    private ImageMetadata extractImageMetadata(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            return ImageMetadata.builder()
                .width(image.getWidth())
                .height(image.getHeight())
                .build();
        } catch (IOException e) {
            log.warn("이미지 메타데이터 추출 실패: {}", e.getMessage());
            return ImageMetadata.builder()
                .width(0)
                .height(0)
                .build();
        }
    }
    
    private void deleteImageFile(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUploadConfig.getUploadDir() + fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
        }
    }
    
    private void createUploadDirectory(String uploadDir) {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + fileExtension;
    }
    
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String getFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
    
    // 이미지 메타데이터 DTO
    @lombok.Builder
    @lombok.Getter
    private static class ImageMetadata {
        private final Integer width;
        private final Integer height;
    }
}
