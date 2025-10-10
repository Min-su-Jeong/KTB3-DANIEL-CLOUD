package com.kakao_tech_bootcamp.community.exception;

import com.kakao_tech_bootcamp.community.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역 예외 처리: 모든 컨트롤러의 예외를 일관된 형식으로 처리하기 위함
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 관련 오류 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(e.getMessage(), null));
    }

    // 시스템 로직 관련 오류 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("시스템 오류가 발생했습니다.", null));
    }
}
