package com.callog.callog_user.common.handler;

import com.callog.callog_user.common.dto.ApiResponseDto;
import com.callog.callog_user.common.exception.ClientError;
import com.callog.callog_user.common.exception.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//예외처리 어노테이션
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 클라이언트 에러처리 (BadParameter, NotFound)
    @ExceptionHandler(ClientError.class)
    public ResponseEntity<ApiResponseDto<String>> handleClientError(ClientError e) {
        ApiResponseDto<String> response = ApiResponseDto.createError(
                e.getErrorCode(),
                e.getErrorMessage()
        );
        HttpStatus status = e instanceof NotFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    //유효성 검증 실패 처리(아이디나 비번이 비었을 때)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<String>> handleValidationError(MethodArgumentNotValidException e) {
        // 첫 번째 검증 실패 메시지 가져오기
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();  // "아이디를 입력하세요." 같은 메시지

        ApiResponseDto<String> response = ApiResponseDto.createError(
                "ValidationError",
                errorMessage
        );

        return ResponseEntity.badRequest().body(response);
    }

    //예상치못한 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleUnexpectedError(Exception e) {
        ApiResponseDto<String> response = ApiResponseDto.createError(
                "InternalServerError",
                "서버 내부 오류가 발생했습니다."
        );

        // 로그에는 실제 에러 내용 기록 (개발자용)
        e.printStackTrace();

        return ResponseEntity.internalServerError().body(response);
    }


}
