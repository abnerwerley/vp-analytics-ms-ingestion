package com.ingestion.vp_analytics.adapters.input.web;

import com.ingestion.vp_analytics.domain.exception.ClientAlreadyExistsException;
import com.ingestion.vp_analytics.domain.exception.DuplicateFileException;
import com.ingestion.vp_analytics.domain.exception.EmptyFileException;
import com.ingestion.vp_analytics.domain.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateFileException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicate(final DuplicateFileException e) {
        return buildError(HttpStatus.CONFLICT, "Arquivo duplicado", e.getMessage());
    }

    @ExceptionHandler(ClientAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicatedUser(final Exception e) {
        return buildError(HttpStatus.CONFLICT, "Usuário já existe", e.getMessage());
    }

    @ExceptionHandler(EmptyFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleEmptyFile(final EmptyFileException e) {
        return buildError(HttpStatus.BAD_REQUEST, "Arquivo inválido", e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleEntityNotFound(final EntityNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnexpected(final Exception e) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", e.getMessage());
    }

    private Map<String, Object> buildError(final HttpStatus status,
                                           final String title, final String detail) {
        final Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        error.put("status", status.value());
        error.put("title", title);
        error.put("detail", detail);
        error.put("message", status.getReasonPhrase());
        return error;
    }
}
