package com.studyhub.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.validation.BindException;

import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리: 표준 에러 포맷 { code, message, fieldErrors? }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ===== 400: Validation/파싱/타입 ===== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofBindingErrors(e.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofBindingErrors(e.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        List<ErrorResponse.FieldError> list = new ArrayList<>();
        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
            list.add(new ErrorResponse.FieldError(field, v.getMessage(), v.getInvalidValue()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", "입력값을 확인해주세요.", list));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String field = e.getName();
        List<ErrorResponse.FieldError> list = List.of(
                new ErrorResponse.FieldError(field, "요청 파라미터 타입이 올바르지 않습니다.", e.getValue()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("TYPE_MISMATCH", "요청 파라미터 타입이 올바르지 않습니다.", list));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("MESSAGE_NOT_READABLE", "요청 본문을 읽을 수 없습니다(형식을 확인해주세요)."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        String field = e.getParameterName();
        List<ErrorResponse.FieldError> list = List.of(
                new ErrorResponse.FieldError(field, "필수 파라미터가 누락되었습니다.", null));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("MISSING_PARAMETER", "필수 파라미터가 누락되었습니다.", list));
    }

    /* ===== 401 / 403 ===== */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("UNAUTHORIZED", "로그인이 필요합니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("FORBIDDEN", "접근 권한이 없습니다."));
    }

    /* ===== 404 ===== (호환: IllegalArgumentException도 404로 매핑) */
    @ExceptionHandler({
            IllegalArgumentException.class,
            NoSuchElementException.class,
            EntityNotFoundException.class,
            EmptyResultDataAccessException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND",
                        e.getMessage() == null ? "요청한 리소스를 찾을 수 없습니다." : e.getMessage()));
    }

    /* ===== 409 ===== */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("CONFLICT",
                        e.getMessage() == null ? "요청 상태가 유효하지 않습니다." : e.getMessage()));
    }

    /* ===== 405 / 415 ===== */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponse.of("UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 요청 형식입니다."));
    }

    /* ===== Spring ErrorResponseException (상태 보존) ===== */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleSpringErrorResponse(ErrorResponseException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String msg = (e.getBody() != null && e.getBody().getDetail() != null)
                ? e.getBody().getDetail() : "요청 처리 중 오류가 발생했습니다.";
        String code = "ERROR_" + status.value();
        return ResponseEntity.status(status).body(ErrorResponse.of(code, msg));
    }

    /* ===== 500: 마지막 가드 ===== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}
