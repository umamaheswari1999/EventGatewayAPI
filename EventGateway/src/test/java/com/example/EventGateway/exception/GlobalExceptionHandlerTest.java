package com.example.EventGateway.exception;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsServiceUnavailableExceptionTo503() {
        assertResponse(handler.handleServiceUnavailable(
                new ServiceUnavailableException("Account service is unavailable.")),
                HttpStatus.SERVICE_UNAVAILABLE,
                "Account service is unavailable.");
    }

    @Test
    void mapsRequestValidationAndBindingErrorsTo400() {
        assertResponse(handler.handleBadRequest(mock(MethodArgumentNotValidException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(BindException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(HttpMessageNotReadableException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(MissingServletRequestParameterException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(MethodArgumentTypeMismatchException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(TypeMismatchException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(ConstraintViolationException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
        assertResponse(handler.handleBadRequest(mock(ServletRequestBindingException.class)),
                HttpStatus.BAD_REQUEST, "Request parameters or body are invalid.");
    }

    @Test
    void mapsMethodMediaTypeAndResponseErrorsToTheirHttpStatuses() {
        assertResponse(handler.handleMethodNotAllowed(mock(HttpRequestMethodNotSupportedException.class)),
                HttpStatus.METHOD_NOT_ALLOWED, "The HTTP method is not supported for this endpoint.");
        assertResponse(handler.handleUnsupportedMediaType(mock(HttpMediaTypeNotSupportedException.class)),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request media type is not supported.");
        assertResponse(handler.handleNotAcceptable(mock(HttpMediaTypeNotAcceptableException.class)),
                HttpStatus.NOT_ACCEPTABLE, "The requested response media type is not available.");
    }

    @Test
    void mapsMissingControllerRoutesToNotFound() {
        assertResponse(handler.handleNotFound(mock(NoHandlerFoundException.class)),
                HttpStatus.NOT_FOUND, "The requested resource was not found.");
        assertResponse(handler.handleNotFound(mock(NoResourceFoundException.class)),
                HttpStatus.NOT_FOUND, "The requested resource was not found.");
    }

    @Test
    void mapsOversizedUploadsToPayloadTooLarge() {
        assertResponse(handler.handleUploadTooLarge(mock(MaxUploadSizeExceededException.class)),
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The uploaded file exceeds the allowed size.");
    }

    @Test
    void mapsAsyncRequestTimeoutToServiceUnavailable() {
        assertResponse(handler.handleRequestTimeout(new AsyncRequestTimeoutException()),
                HttpStatus.SERVICE_UNAVAILABLE, "The request timed out.");
    }

    @Test
    void preservesControllerResponseStatusAndProvidesDefaultDetail() {
        assertResponse(
                handler.handleResponseStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.")),
                HttpStatus.NOT_FOUND,
                "Event not found.");
        assertResponse(
                handler.handleResponseStatus(new ResponseStatusException(HttpStatus.BAD_REQUEST)),
                HttpStatus.BAD_REQUEST,
                "The request could not be completed.");
    }

    @Test
    void mapsDataIntegrityFailureToConflictWithoutLeakingDatabaseDetails() {
        assertResponse(
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("sensitive SQL details")),
                HttpStatus.CONFLICT,
                "The request conflicts with existing data.");
    }

    @Test
    void mapsConcurrentDataChangeToConflict() {
        assertResponse(
                handler.handleConcurrencyFailure(new ConcurrencyFailureException("concurrent update")),
                HttpStatus.CONFLICT,
                "The data changed while the request was being processed.");
    }

    @Test
    void mapsDatabaseResourceFailureToServiceUnavailable() {
        assertResponse(
                handler.handleDatabaseUnavailable(new DataAccessResourceFailureException("connection failed")),
                HttpStatus.SERVICE_UNAVAILABLE,
                "The database is currently unavailable.");
    }

    @Test
    void mapsSqlAndPersistenceErrorsToInternalServerErrorWithoutLeakingDetails() {
        assertResponse(handler.handleDatabaseError(mock(DataAccessException.class)),
                HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
        assertResponse(handler.handleDatabaseError(new SQLException("sensitive SQL details")),
                HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
        assertResponse(handler.handleDatabaseError(new PersistenceException("sensitive SQL details")),
                HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
        assertResponse(handler.handleDatabaseError(mock(TransactionException.class)),
                HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
    }

    @Test
    void mapsUnexpectedExceptionToInternalServerErrorWithoutLeakingDetails() {
        assertResponse(
                handler.handleUnexpectedException(new IllegalStateException("internal detail")),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.");
    }

    private void assertResponse(
            ResponseEntity<ProblemDetail> response,
            HttpStatusCode expectedStatus,
            String expectedDetail) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(expectedStatus.value(), response.getBody().getStatus());
        if (expectedDetail != null) {
            assertEquals(expectedDetail, response.getBody().getDetail());
        }
    }
}
