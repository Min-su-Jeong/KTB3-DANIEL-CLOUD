package com.kakao_tech_bootcamp.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostImageId {
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "image_id")
    private Integer imageId;
}
