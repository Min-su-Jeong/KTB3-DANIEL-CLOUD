package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.dto.PostResponses;
import com.kakao_tech_bootcamp.community.entity.PostStat;
import com.kakao_tech_bootcamp.community.repository.PostStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 게시글 통계 비즈니스 로직: 좋아요/조회수 관리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostStatService {

    private final PostStatRepository postStatRepository;

    // 게시글 통계 조회
    public PostResponses.PostStatResponse getPostStats(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            // 통계가 없으면 기본값으로 생성
            stat = initializePostStat(postId);
        }
        
        return new PostResponses.PostStatResponse(
                stat.getPostId(),
                stat.getLikeCount(),
                stat.getViewCount(),
                stat.getCommentCount()
        );
    }

    // 게시글 통계 초기화
    @Transactional
    public PostStat initializePostStat(Integer postId) {
        PostStat stat = new PostStat(postId);
        return postStatRepository.save(stat);
    }

    // 조회수 증가
    @Transactional
    public void incrementViewCount(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            stat = initializePostStat(postId);
        }
        postStatRepository.incrementViewCount(postId);
    }

    // 좋아요 개수 증가
    @Transactional
    public void incrementLikeCount(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            stat = initializePostStat(postId);
        }
        postStatRepository.incrementLikeCount(postId);
    }

    // 좋아요 개수 감소
    @Transactional
    public void decrementLikeCount(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            return; // 통계가 없으면 감소할 것도 없음
        }
        postStatRepository.decrementLikeCount(postId);
    }

    // 댓글 개수 증가
    @Transactional
    public void incrementCommentCount(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            stat = initializePostStat(postId);
        }
        postStatRepository.incrementCommentCount(postId);
    }

    // 댓글 개수 감소
    @Transactional
    public void decrementCommentCount(Integer postId) {
        PostStat stat = postStatRepository.findByPostId(postId);
        if (stat == null) {
            return; // 통계가 없으면 감소할 것도 없음
        }
        postStatRepository.decrementCommentCount(postId);
    }

    // 게시글 삭제 시 통계도 삭제
    @Transactional
    public void deleteByPostId(Integer postId) {
        postStatRepository.deleteByPostId(postId);
    }
}
