package com.kakao_tech_bootcamp.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PostLike {

    @EmbeddedId
    private PostLikeId id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public PostLike(Integer userId, Integer postId) {
        this.id = new PostLikeId(userId, postId);
    }

    // 좋아요 삭제를 위한 메서드
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}