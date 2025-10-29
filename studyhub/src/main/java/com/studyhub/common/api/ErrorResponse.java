package com.studyhub.common.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;

/**
 * 표준 에러 응답 DTO: { code, message, fieldErrors? }
 * - code: 기계 친화 코드 (예: VALIDATION_ERROR, UNAUTHORIZED)
 * - message: 사람 친화 메시지
 * - fieldErrors: 필드 단위 검증 오류 목록
 */
public class ErrorResponse {

    private String code;
    private String message;
    private List<FieldError> fieldErrors;

    public ErrorResponse() {}

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }

    public static ErrorResponse of(String code, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(code, message, fieldErrors);
    }

    /** Spring BindingResult -> fieldErrors 변환 */
    public static ErrorResponse ofBindingErrors(BindingResult bindingResult) {
        List<FieldError> list = new ArrayList<>();
        for (org.springframework.validation.FieldError fe : bindingResult.getFieldErrors()) {
            list.add(new FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()));
        }
        // ✅ 코드/메시지 통일
        return new ErrorResponse("VALIDATION_ERROR", "입력값을 확인해주세요.", list);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

    /** 필드 단위 오류 */
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;

        public FieldError() {}

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
    }
}
