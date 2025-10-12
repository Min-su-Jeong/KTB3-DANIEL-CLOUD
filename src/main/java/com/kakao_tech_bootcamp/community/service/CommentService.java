package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.Comment;
import com.kakao_tech_bootcamp.community.entity.Post;
import com.kakao_tech_bootcamp.community.entity.User;
import com.kakao_tech_bootcamp.community.dto.CommentRequests;
import com.kakao_tech_bootcamp.community.dto.CommentResponses;
import com.kakao_tech_bootcamp.community.repository.CommentRepository;
import com.kakao_tech_bootcamp.community.repository.PostRepository;
import com.kakao_tech_bootcamp.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 댓글 비즈니스 로직: 댓글 CRUD, 대댓글 처리, 계층 구조 관리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostStatService postStatService;

    // 댓글 작성
    @Transactional
    public CommentResponses.CommentResponse createComment(Integer postId, Integer userId, CommentRequests.CreateCommentRequest req) {
        // 댓글 데이터 검증
        validateCreateCommentRequest(req);

        // Post와 User 엔티티 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 댓글 생성
        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parentId(req.parentId())
                .content(req.content())
                .depth(req.parentId() != null ? 1 : 0) // 대댓글은 깊이 1, 일반 댓글은 깊이 0
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 게시글 댓글 개수 증가
        postStatService.incrementCommentCount(post.getPostId());

        log.info("댓글 작성 완료: commentId={}, postId={}, userId={}", 
                savedComment.getCommentId(), savedComment.getPost().getPostId(), savedComment.getUser().getUserId());

        return new CommentResponses.CommentResponse(
                savedComment.getCommentId(),
                savedComment.getPost().getPostId(),
                savedComment.getUser().getUserId(),
                savedComment.getParentId(),
                savedComment.getContent(),
                savedComment.getDepth(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    // 게시글의 댓글 목록 조회 (계층 구조)
    public CommentResponses.CommentListResponse getCommentsByPostId(Integer postId) {

        List<Comment> comments = commentRepository.findByPostIdOrderByParentIdAscCreatedAtAsc(postId);
        
        // 계층 구조로 변환
        List<CommentResponses.CommentResponse> commentResponses = comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(comment -> new CommentResponses.CommentResponse(
                        comment.getCommentId(),
                        comment.getPost().getPostId(),
                        comment.getUser().getUserId(),
                        comment.getParentId(),
                        comment.getContent(),
                        comment.getDepth(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt(),
                        comments.stream()
                                .filter(reply -> comment.getCommentId().equals(reply.getParentId()))
                                .map(reply -> new CommentResponses.CommentResponse(
                                        reply.getCommentId(),
                                        reply.getPost().getPostId(),
                                        reply.getUser().getUserId(),
                                        reply.getParentId(),
                                        reply.getContent(),
                                        reply.getDepth(),
                                        reply.getCreatedAt(),
                                        reply.getUpdatedAt(),
                                        new ArrayList<>()
                                ))
                                .toList()
                ))
                .toList();

        Long totalCount = commentRepository.countByPostId(postId);

        return new CommentResponses.CommentListResponse(commentResponses, totalCount);
    }

    // 특정 댓글 조회
    public CommentResponses.CommentResponse getComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 대댓글 목록 조회
        List<Comment> replies = commentRepository.findByPostIdOrderByParentIdAscCreatedAtAsc(comment.getPost().getPostId())
                .stream()
                .filter(c -> commentId.equals(c.getParentId()))
                .toList();

        return new CommentResponses.CommentResponse(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getUser().getUserId(),
                comment.getParentId(),
                comment.getContent(),
                comment.getDepth(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies.stream()
                        .map(reply -> new CommentResponses.CommentResponse(
                                reply.getCommentId(),
                                reply.getPost().getPostId(),
                                reply.getUser().getUserId(),
                                reply.getParentId(),
                                reply.getContent(),
                                reply.getDepth(),
                                reply.getCreatedAt(),
                                reply.getUpdatedAt(),
                                new ArrayList<>()
                        ))
                        .toList()
        );
    }

    // 댓글 수정
    @Transactional
    public CommentResponses.CommentResponse updateComment(Integer commentId, CommentRequests.UpdateCommentRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // TODO: 현재 로그인 사용자 ID와 comment.getUser().getUserId() 비교 필요

        // 댓글 수정 데이터 검증
        validateUpdateCommentRequest(req);

        comment.updateContent(req.content());
        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 수정 완료: commentId={}", commentId);

        return new CommentResponses.CommentResponse(
                savedComment.getCommentId(),
                savedComment.getPost().getPostId(),
                savedComment.getUser().getUserId(),
                savedComment.getParentId(),
                savedComment.getContent(),
                savedComment.getDepth(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // TODO: 현재 로그인 사용자 ID와 comment.getUser().getUserId() 비교 필요

        // 댓글과 대댓글들 모두 hard delete
        commentRepository.deleteCommentAndReplies(commentId);

        // 게시글 댓글 개수 감소
        postStatService.decrementCommentCount(comment.getPost().getPostId());

        log.info("댓글 삭제 완료: commentId={}", commentId);
    }

    // 댓글 통계 조회
    public CommentResponses.CommentStatsResponse getCommentStats(Integer postId) {

        Long totalCommentCount = commentRepository.countByPostId(postId);
        Long replyCount = commentRepository.countByParentId(postId);

        return new CommentResponses.CommentStatsResponse(postId, totalCommentCount, replyCount);
    }

    // 사용자의 댓글 목록 조회
    public List<CommentResponses.CommentSummaryResponse> getUserComments(Integer userId) {

        List<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return comments.stream()
                .map(comment -> new CommentResponses.CommentSummaryResponse(
                        comment.getCommentId(),
                        comment.getUser().getUserId(),
                        comment.getContent(),
                        comment.getDepth(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    // ========== 검증 메서드 ==========

    private void validateCreateCommentRequest(CommentRequests.CreateCommentRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
        if (req.content().length() > 500) {
            throw new IllegalArgumentException("댓글은 500자를 초과할 수 없습니다.");
        }
    }

    private void validateUpdateCommentRequest(CommentRequests.UpdateCommentRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
        if (req.content().length() > 500) {
            throw new IllegalArgumentException("댓글은 500자를 초과할 수 없습니다.");
        }
    }
}
