package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.ArityMismatchException;
import com.paulrule.mathapi.domain.Operation;
import com.paulrule.mathapi.domain.UnknownOperationException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** The single source of error responses: RFC 9457 Problem Details, including framework errors (STANDARDS.md §4). */
@RestControllerAdvice
class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    private static final String SUPPORTED =
            Arrays.stream(Operation.values()).map(Operation::name).collect(Collectors.joining(", "));

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + type));
        problem.setTitle(title);
        return problem;
    }

    /** RFC 9457's default type, written out: Spring omits a null type and PRD §3.2 wants one on every error. */
    private static final URI BLANK = URI.create("about:blank");

    private static ProblemDetail invalidRequest(String detail) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", detail);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (body instanceof ProblemDetail problem && problem.getType() == null) {
            problem.setType(BLANK);
        }
        return super.createResponseEntity(body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(invalidRequest(
                        "The request body must be a JSON object whose operands are decimal numbers or strings."));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(invalidRequest("The fields 'operation' and 'a' are required."));
    }

    @ExceptionHandler
    ProblemDetail unknownOperation(UnknownOperationException ex) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "unknown-operation",
                "Unknown operation",
                "Unknown operation. Supported operations: " + SUPPORTED + ".");
    }

    @ExceptionHandler
    ProblemDetail arityMismatch(ArityMismatchException ex) {
        return invalidRequest(ex.getMessage());
    }

    /** Anything unplanned is still Problem Details, and says nothing about why. */
    @ExceptionHandler
    ProblemDetail unexpected(Exception ex) {
        logger.error("Unexpected error", ex);
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "The server could not complete the request.");
        problem.setType(BLANK);
        return problem;
    }
}
