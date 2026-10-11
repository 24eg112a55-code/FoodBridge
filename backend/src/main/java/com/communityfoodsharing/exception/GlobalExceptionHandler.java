package com.communityfoodsharing.exception;

import com.communityfoodsharing.dto.Dtos.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> business(BusinessException ex) { return response(ex.getStatus(), ex.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorResponse> validation(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException invalid
            ? invalid.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; "))
            : ex.getMessage();
        return response(400, message == null || message.isBlank() ? "Invalid request" : message);
    }
    @ExceptionHandler({BadCredentialsException.class})
    ResponseEntity<ErrorResponse> credentials(Exception ex) { return response(401, "Invalid email or password"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException ex) { return response(409, "A record with that unique value already exists"); }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> illegalArgument(Exception ex) { return response(400, ex.getMessage()); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex) { return response(500, "An unexpected server error occurred"); }
    private ResponseEntity<ErrorResponse> response(int status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, message, LocalDateTime.now()));
    }
}