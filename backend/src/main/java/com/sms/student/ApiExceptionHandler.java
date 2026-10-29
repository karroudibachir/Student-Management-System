package com.sms.student;

import jakarta.servlet.http.HttpServletRequest;
import com.sms.auth.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice public class ApiExceptionHandler {
 record FieldIssue(String field,String message){}
 record ErrorBody(OffsetDateTime timestamp,int status,String error,String message,String path,List<FieldIssue> fieldErrors){}
 private ErrorBody body(HttpStatus s,String message,HttpServletRequest r,List<FieldIssue> fields){return new ErrorBody(OffsetDateTime.now(),s.value(),s.getReasonPhrase(),message,r.getRequestURI(),fields);}
 @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ErrorBody> notFound(ResourceNotFoundException e,HttpServletRequest r){return ResponseEntity.status(404).body(body(HttpStatus.NOT_FOUND,e.getMessage(),r,List.of()));}
 @ExceptionHandler(DuplicateEmailException.class) ResponseEntity<ErrorBody> duplicate(DuplicateEmailException e,HttpServletRequest r){return ResponseEntity.status(409).body(body(HttpStatus.CONFLICT,e.getMessage(),r,List.of()));}
 @ExceptionHandler(InvalidCredentialsException.class) ResponseEntity<ErrorBody> invalidCredentials(InvalidCredentialsException e,HttpServletRequest r){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED,e.getMessage(),r,List.of()));}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ErrorBody> badRequest(IllegalArgumentException e,HttpServletRequest r){return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST,e.getMessage(),r,List.of()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorBody> invalid(MethodArgumentNotValidException e,HttpServletRequest r){var errors=e.getBindingResult().getFieldErrors().stream().map(x->new FieldIssue(x.getField(),Objects.requireNonNullElse(x.getDefaultMessage(),"Invalid value"))).toList();return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST,"Validation failed",r,errors));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorBody> integrity(DataIntegrityViolationException e,HttpServletRequest r){return ResponseEntity.status(409).body(body(HttpStatus.CONFLICT,"A record with the supplied unique value already exists",r,List.of()));}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorBody> unexpected(Exception e,HttpServletRequest r){return ResponseEntity.status(500).body(body(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred",r,List.of()));}
}
