package com.umdev.infoeste.utils.handler;

import com.umdev.infoeste.utils.exceptions.*;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(ex, request, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                "Validation failed: " + ex.getMessage(), 
                request, 
                HttpStatus.BAD_REQUEST
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                "Invalid email or password", 
                request, 
                HttpStatus.UNAUTHORIZED
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(StoreAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleStoreAlreadyExists(
            StoreAlreadyExistsException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                ex.getMessage(), 
                request, 
                HttpStatus.CONFLICT
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                ex.getMessage(), 
                request, 
                HttpStatus.NOT_FOUND
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFile(
            InvalidFileException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                ex.getMessage(), 
                request, 
                HttpStatus.BAD_REQUEST
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleFileProcessing(
            FileProcessingException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                ex.getMessage(), 
                request, 
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                ex.getMessage(), 
                request, 
                HttpStatus.NOT_FOUND
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        HttpStatus status;
        
        String message = ex.getMessage().toLowerCase();
        if (message.contains("already exists") || message.contains("already registered")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("not found") || message.contains("doesn't belong")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.contains("file") || message.contains("image") || 
                   message.contains("size") || message.contains("type")) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        
        ExceptionResponse response = ExceptionResponse.of(ex.getMessage(), request, status);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                "Internal processing error: " + ex.getMessage(), 
                request, 
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponse> handleExpiredJwt(
            ExpiredJwtException ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                "Token expired. Please login again.", 
                request, 
                HttpStatus.UNAUTHORIZED
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse response = ExceptionResponse.of(
                "File size exceeds maximum allowed limit",
                request,
                HttpStatus.PAYLOAD_TOO_LARGE
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        ExceptionResponse response = ExceptionResponse.of(
                "An unexpected error occurred", 
                request, 
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
