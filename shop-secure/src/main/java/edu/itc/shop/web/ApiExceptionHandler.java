package edu.itc.shop.web;

import edu.itc.shop.service.ProductNotFoundException;
import edu.itc.shop.service.UsernameTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail notFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not Found");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Bad Request");
        List<FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldValidationError(error.getField(),
                        String.valueOf(error.getDefaultMessage())))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(UsernameTakenException.class)
    ProblemDetail usernameTaken(UsernameTakenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflict");
        return problem;
    }

    private record FieldValidationError(String field, String message) {
    }
}