package com.kakao_tech_bootcamp.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "post_stat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStat {

    @Id
    @Column(name = "post_id")
    private Integer postId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Builder
    public PostStat(Integer postId) {
        this.postId = postId;
        this.likeCount = 0;
        this.viewCount = 0;
        this.commentCount = 0;
    }
}
