package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.Image;
import com.kakao_tech_bootcamp.community.entity.Post;
import com.kakao_tech_bootcamp.community.entity.PostImage;
import com.kakao_tech_bootcamp.community.entity.User;
import com.kakao_tech_bootcamp.community.repository.ImageRepository;
import com.kakao_tech_bootcamp.community.repository.PostImageRepository;
import com.kakao_tech_bootcamp.community.repository.PostRepository;
import com.kakao_tech_bootcamp.community.repository.UserRepository;
import com.kakao_tech_bootcamp.community.dto.ImageRequests;
import com.kakao_tech_bootcamp.community.dto.ImageResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    // TODO: 임시로 로컬 파일 시스템 사용 - 추후 S3로 변경 예정
    // 파일 업로드 설정 (로컬 파일 시스템)
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    private final ImageRepository imageRepository;
    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 이미지 업로드 (중복 체크 포함)
    @Transactional
    public Image uploadImage(Integer userId, MultipartFile file) {
        // 파일 검증
        validateImageFile(file);
        
        // 파일 업로드
        String fileUrl = uploadImageFile(file);
        
        // 중복 체크
        Image existingImage = imageRepository.findByFileUrl(fileUrl).orElse(null);
        if (existingImage != null) {
            log.info("중복 이미지 발견, 기존 이미지 반환: imageId={}, fileUrl={}", existingImage.getImageId(), fileUrl);
            return existingImage;
        }
        
        // 썸네일 생성
        String thumbnailUrl = generateThumbnail(fileUrl);
        
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
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
    }

    // 사용자 이미지 목록 조회
    public List<Image> getUserImages(Integer userId) {
        return imageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 이미지 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        
        // 파일 삭제
        deleteImageFile(image.getFileUrl());
        if (image.getThumbnailUrl() != null) {
            deleteImageFile(image.getThumbnailUrl());
        }
        
        // hard delete
        imageRepository.delete(image);
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
            
            // hard delete
            imageRepository.delete(image);
        }
        
        log.info("사용하지 않는 이미지 정리 완료: {}개", unusedImages.size());
    }

    // ========== 프로필 이미지 관련 기능 ==========

    // 프로필 이미지 업로드
    @Transactional
    public String uploadProfileImage(Integer userId, MultipartFile file) {
        // 사용자 존재 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // ImageService를 통해 이미지 업로드 (중복 체크 포함)
        Image image = uploadImage(userId, file);
        
        // 기존 프로필 이미지가 있다면 삭제
        if (user.getProfileImage() != null) {
            deleteImage(user.getProfileImage().getImageId());
        }
        
        // User 엔티티 업데이트
        user.updateProfileImage(image);
        userRepository.save(user);
        
        log.info("프로필 이미지 업로드 완료: userId={}, imageId={}", userId, image.getImageId());
        return image.getFileUrl();
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(Integer userId) {
        // 사용자 존재 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getProfileImage() == null) {
            throw new IllegalArgumentException("삭제할 프로필 이미지가 없습니다.");
        }

        // ImageService를 통해 이미지 삭제
        deleteImage(user.getProfileImage().getImageId());
        
        // DB에서 프로필 이미지 관계 제거
        user.removeProfileImage();
        userRepository.save(user);
        
        log.info("프로필 이미지 삭제 완료: userId={}", userId);
    }

    // 프로필 이미지 URL 조회
    public String getProfileImageUrl(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return user.getProfileImage() != null ? user.getProfileImage().getFileUrl() : null;
    }

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
            Image image = uploadImage(postId, file);
            
            // 다음 순서 계산
            Integer nextOrder = postImageRepository.findNextImageOrder(postId);
            
            // Post 엔티티 조회
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
            
            // PostImage 엔티티 생성 및 저장
            PostImage postImage = PostImage.builder()
                .post(post)
                .imageUrl(image.getFileUrl())
                .imageOrder(nextOrder)
                .build();
            
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
        
        List<PostImage> postImages = postImageRepository.findByPostIdOrderByImageOrderAsc(postId);
        
        return postImages.stream()
                .map(postImage -> new ImageResponses.PostImageResponse(
                    postImage.getImageId(),
                    postImage.getPost().getPostId(),
                    postImage.getImageUrl(),
                    postImage.getImageOrder(),
                    postImage.getCreatedAt()
                ))
                .toList();
    }

    // 게시글 이미지 삭제
    @Transactional
    public void removeImageFromPost(Integer imageId) {
        // PostImage 조회 및 삭제 (hard delete)
        PostImage postImage = postImageRepository.findByImageId(imageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
        
        postImageRepository.delete(postImage);
        
        log.info("게시글 이미지 삭제 완료: imageId={}", imageId);
    }

    // 게시글 이미지 순서 변경
    @Transactional
    public void updatePostImageOrder(Integer imageId, ImageRequests.UpdateImageOrderRequest req) {
        // PostImage 조회
        PostImage postImage = postImageRepository.findByImageId(imageId)
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
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다");
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }
    }
    
    // TODO: 로컬 파일 시스템 업로드 - 추후 S3로 변경 예정
    private String uploadImageFile(MultipartFile file) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String uploadDir = UPLOAD_DIR + "/images";
            createUploadDirectory(uploadDir);
            
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return "/images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다", e);
        }
    }
    
    private String generateThumbnail(String fileUrl) {
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
    
    // TODO: 로컬 파일 시스템 삭제 - 추후 S3로 변경 예정
    private void deleteImageFile(String fileUrl) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + fileUrl);
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
}