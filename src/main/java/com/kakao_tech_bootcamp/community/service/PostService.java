package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.Post;
import com.kakao_tech_bootcamp.community.dto.PostRequests;
import com.kakao_tech_bootcamp.community.dto.PostResponses;
import com.kakao_tech_bootcamp.community.repository.PostRepository;
import com.kakao_tech_bootcamp.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 게시글 비즈니스 로직: 작성/조회/수정/삭제/검색 처리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPost(PostRequests.CreatePostRequest req) {
        // 사용자 존재 여부 검증
        validateUserExists(req.userId());
        
        // 게시글 데이터 검증
        validateCreatePostRequest(req);
        
        postRepository.save(Post.builder()
                .userId(req.userId())
                .title(req.title())
                .content(req.content())
                .build());
    }

    public PostResponses.PostResponse getPost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 입문자용: 직접 생성자 호출 (팩토리 메서드 대신)
        return new PostResponses.PostResponse(
            post.getPostId(),
            post.getUserId(),
            post.getTitle(),
            post.getContent(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }

    // 인피니티 스크롤용 게시글 조회
    public List<PostResponses.PostSummaryResponse> getPosts(Integer lastPostId, Integer limit) {
        List<Post> posts;
        
        if (lastPostId == null) {
            // 첫 페이지: lastPostId가 없으면 최신 게시글부터
            posts = postRepository.findFirstPagePosts(org.springframework.data.domain.PageRequest.of(0, limit));
        } else {
            // 다음 페이지: lastPostId 이후의 게시글들
            posts = postRepository.findPostsAfterId(lastPostId, org.springframework.data.domain.PageRequest.of(0, limit));
        }
        return posts.stream()
                .map(post -> new PostResponses.PostSummaryResponse(
                    post.getPostId(),
                    post.getUserId(),
                    post.getTitle(),
                    post.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void updatePost(Integer postId, PostRequests.UpdatePostRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 게시글 수정 권한 확인 (작성자만 수정 가능)
        // TODO: 실제 로그인 시스템 구현 시 권한 검증 로직 추가 필요
        
        // 게시글 데이터 검증
        validateUpdatePostRequest(req);

        if (req.title() != null) post.setTitle(req.title());
        if (req.content() != null) post.setContent(req.content());

        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 게시글 삭제 권한 확인 (작성자만 삭제 가능)
        // TODO: 실제 로그인 시스템 구현 시 권한 검증 로직 추가 필요

        postRepository.delete(post);
    }

    // 사용자 존재 여부 검증
    private void validateUserExists(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
    }

    // 게시글 생성 요청 검증
    private void validateCreatePostRequest(PostRequests.CreatePostRequest req) {
        if (req.title() == null || req.title().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        if (req.title().length() > 50) {
            throw new IllegalArgumentException("제목은 50자 이하로 입력해주세요.");
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (req.content().length() > 10000) {
            throw new IllegalArgumentException("내용은 10,000자 이하로 입력해주세요.");
        }
    }

    // 게시글 수정 요청 검증
    private void validateUpdatePostRequest(PostRequests.UpdatePostRequest req) {
        if (req.title() != null && req.title().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        if (req.title() != null && req.title().length() > 50) {
            throw new IllegalArgumentException("제목은 50자 이하로 입력해주세요.");
        }
        if (req.content() != null && req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (req.content() != null && req.content().length() > 10000) {
            throw new IllegalArgumentException("내용은 10,000자 이하로 입력해주세요.");
        }
    }
}