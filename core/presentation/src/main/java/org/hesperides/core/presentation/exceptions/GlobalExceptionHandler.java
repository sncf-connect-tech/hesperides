package org.hesperides.core.presentation.exceptions;

import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.modules.exceptions.UpdateReleaseException;
import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyWithDefaultValueException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

/**
 * Centralisation de la gestion des exceptions (bien pratique)
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * se produit quand on veut executer une commande sur un aggregat qui n'existe pas.
     *
     * @param ex "not found" failure
     * @return entity
     */
    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity handleAggregateNotFound(AggregateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getAggregateIdentifier() + ": " + ex.getMessage());
    }

    @ExceptionHandler({DuplicateException.class, OutOfDateVersionException.class})
    public ResponseEntity handleConflict(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, RequiredPropertyWithDefaultValueException.class, UpdateReleaseException.class})
    public ResponseEntity handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * se produit sur les queries.
     *
     * @param ex "not found" failure
     * @return entitiy
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body != null ? body : ex.getMessage(), headers, status);
    }
}
