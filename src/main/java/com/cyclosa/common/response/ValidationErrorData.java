package com.cyclosa.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidationErrorData {

    private List<FieldError> errors;

    public static ValidationErrorData of(List<FieldError> errors) {
        return ValidationErrorData.builder().errors(errors).build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;

        public static FieldError of(String field, String message) {
            return FieldError.builder().field(field).message(message).build();
        }
    }
}
