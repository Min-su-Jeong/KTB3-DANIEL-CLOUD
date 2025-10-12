package com.kakao_tech_bootcamp.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PostImage(Post post, String imageUrl, Integer imageOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder != null ? imageOrder : 0;
    }


    // 이미지 URL 수정을 위한 메서드
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 이미지 순서 수정을 위한 메서드
    public void updateOrder(Integer imageOrder) {
        this.imageOrder = imageOrder;
    }
}