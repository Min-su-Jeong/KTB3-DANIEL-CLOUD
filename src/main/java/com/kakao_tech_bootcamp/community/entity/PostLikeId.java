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
@EqualsAndHashCode // JPA 복합키 비교 시 사용
public class PostLikeId {
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "post_id")
	private Integer postId;
}