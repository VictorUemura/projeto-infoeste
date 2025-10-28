package com.umdev.infoeste.utils.handler;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponse(
        ZonedDateTime timestamp,
        String path,
        int status,
        String error,
        String message,
        Set<ValidationError> validationErrors
) {

    public record ValidationError(String field, String message) {
    }

    private static Set<ValidationError> extractErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError ->
                        new ValidationError(fieldError.getField(),
                                fieldError.getDefaultMessage()))
                .collect(Collectors.toSet());
    }

    public static ExceptionResponse of(Exception ex, WebRequest request, HttpStatus status) {
        Set<ValidationError> validationErrors = null;
        String message = ex.getMessage();

        if (ex instanceof MethodArgumentNotValidException exValidation) {
            validationErrors = extractErrors(exValidation);
            message = "Validation failed for one or more fields.";
        }

        return new ExceptionResponse(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")),
                request.getDescription(false),
                status.value(),
                status.getReasonPhrase(),
                message,
                validationErrors);
    }

    public static ExceptionResponse of(String message, WebRequest request, HttpStatus status) {
        return new ExceptionResponse(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")),
                request.getDescription(false),
                status.value(),
                status.getReasonPhrase(),
                message,
                null);
    }

}