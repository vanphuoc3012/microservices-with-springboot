package com.microservices.util;

import com.microservices.api.exception.InvalidInputException;
import com.microservices.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(
            ServerHttpRequest request, NotFoundException exception) {

        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, exception);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputException(
            ServerHttpRequest request, InvalidInputException e) {

        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, e);
    }

    private HttpErrorInfo createHttpErrorInfo(
            HttpStatus status, ServerHttpRequest request, Exception exception) {
        final String path = request.getPath().pathWithinApplication().value();
        final String message = exception.getMessage();
        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", status, path, message);

        return new HttpErrorInfo(path, status, message);
    }
}
