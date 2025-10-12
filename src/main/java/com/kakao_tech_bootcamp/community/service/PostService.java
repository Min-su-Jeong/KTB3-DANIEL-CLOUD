package com.kakao_tech_bootcamp.community.service;

import com.kakao_tech_bootcamp.community.entity.Post;
import com.kakao_tech_bootcamp.community.entity.User;
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
        if (req.title() == null || req.title().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        
        // User 엔티티 조회
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        postRepository.save(Post.builder()
                .user(user)
                .title(req.title())
                .content(req.content())
                .build());
    }

    public PostResponses.PostResponse getPost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        return new PostResponses.PostResponse(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                java.util.Collections.emptyList(), // images
                "작성자", // authorNickname
                null, // authorProfileImageUrl
                0, // likeCount
                0, // commentCount
                0 // viewCount
        );
    }

    public List<PostResponses.PostSummaryResponse> getPosts(Integer lastPostId, Integer limit) {
        List<Post> posts;
        
        if (lastPostId == null) {
            posts = postRepository.findFirstPagePosts(org.springframework.data.domain.PageRequest.of(0, limit));
        } else {
            posts = postRepository.findPostsAfterId(lastPostId, org.springframework.data.domain.PageRequest.of(0, limit));
        }
        
        return posts.stream()
                .map(post -> new PostResponses.PostSummaryResponse(
                    post.getPostId(),
                    post.getUser().getUserId(),
                    post.getTitle(),
                    post.getCreatedAt(),
                    0, // likeCount
                    0, // commentCount
                    0, // viewCount
                    "작성자", // authorNickname
                    null // authorProfileImageUrl
                ))
                .toList();
    }

    @Transactional
    public void updatePost(Integer postId, PostRequests.UpdatePostRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (req.title() != null && req.title().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        if (req.content() != null && req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        post.updateContent(req.title(), req.content());
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        postRepository.delete(post);
    }
}