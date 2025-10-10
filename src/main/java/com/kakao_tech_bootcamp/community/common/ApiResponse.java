package com.kakao_tech_bootcamp.community.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 공통 API 응답 형식
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data;
}