package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.PostLike;
import com.kakao_tech_bootcamp.community.dto.PostResponses;
import com.kakao_tech_bootcamp.community.repository.PostLikeRepository;
import com.kakao_tech_bootcamp.community.repository.PostRepository;
import com.kakao_tech_bootcamp.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 게시글 좋아요 비즈니스 로직: 좋아요 추가/삭제/조회 처리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostStatService postStatService;

    @Transactional
    public void addPostLike(Integer userId, Integer postId) {
        // 사용자와 게시글 존재 여부 검증
        validateUserExists(userId);
        validatePostExists(postId);
        
        // 이미 좋아요를 눌렀는지 확인
        boolean isAlreadyLiked = postLikeRepository.existsByUserIdAndPostIdAndDeletedAtIsNull(userId, postId);
        if (isAlreadyLiked) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }
        
        // 좋아요 추가
        PostLike newLike = new PostLike(userId, postId);
        postLikeRepository.save(newLike);
        
        // 통계 업데이트
        postStatService.incrementLikeCount(postId);
        
        log.info("게시글 좋아요 추가 완료: userId={}, postId={}", userId, postId);
    }

    @Transactional
    public void removePostLike(Integer userId, Integer postId) {
        // 사용자와 게시글 존재 여부 검증
        validateUserExists(userId);
        validatePostExists(postId);
        
        // 기존 좋아요 확인
        PostLike existingLike = postLikeRepository.findByUserIdAndPostIdAndDeletedAtIsNull(userId, postId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않은 게시글입니다."));
        
        // Soft delete: deletedAt 설정
        existingLike.markAsDeleted();
        postLikeRepository.save(existingLike);
        
        // 통계 업데이트
        postStatService.decrementLikeCount(postId);
        
        log.info("게시글 좋아요 삭제 완료: userId={}, postId={}", userId, postId);
    }

    public PostResponses.LikeStatusResponse getPostLikeStatus(Integer userId, Integer postId) {
        // 사용자와 게시글 존재 여부 검증
        validateUserExists(userId);
        validatePostExists(postId);
        
        // 좋아요 상태 확인
        boolean isLiked = postLikeRepository.existsByUserIdAndPostIdAndDeletedAtIsNull(userId, postId);
        
        // 좋아요 개수 조회
        long likeCount = postLikeRepository.countByPostIdAndDeletedAtIsNull(postId);
        
        return new PostResponses.LikeStatusResponse(isLiked, (int) likeCount);
    }

    public int getPostLikeCount(Integer postId) {
        // 게시글 존재 여부 검증
        validatePostExists(postId);
        
        return (int) postLikeRepository.countByPostIdAndDeletedAtIsNull(postId);
    }
    
    private void validateUserExists(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
    }

    private void validatePostExists(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("게시글 ID가 필요합니다.");
        }
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
    }
}
