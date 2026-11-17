package com.example.EventGateway.exception;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleServiceUnavailable(ServiceUnavailableException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            ConstraintViolationException.class,
            ServletRequestBindingException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(Exception exception) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "The HTTP method is not supported for this endpoint.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(Exception exception) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request media type is not supported.");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleNotAcceptable(Exception exception) {
        return problem(HttpStatus.NOT_ACCEPTABLE, "The requested response media type is not available.");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(Exception exception) {
        return problem(HttpStatus.NOT_FOUND, "The requested resource was not found.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleUploadTooLarge(Exception exception) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "The uploaded file exceeds the allowed size.");
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ProblemDetail> handleRequestTimeout(Exception exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "The request timed out.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception) {
        String detail = exception.getReason() == null
                ? "The request could not be completed."
                : exception.getReason();
        return problem(exception.getStatusCode(), detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return problem(HttpStatus.CONFLICT, "The request conflicts with existing data.");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrencyFailure(ConcurrencyFailureException exception) {
        return problem(HttpStatus.CONFLICT, "The data changed while the request was being processed.");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ProblemDetail> handleDatabaseUnavailable(DataAccessResourceFailureException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "The database is currently unavailable.");
    }

    @ExceptionHandler({
            DataAccessException.class,
            SQLException.class,
            PersistenceException.class,
            TransactionException.class
    })
    public ResponseEntity<ProblemDetail> handleDatabaseError(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatusCode status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        return ResponseEntity.status(status).body(problem);
    }
}
