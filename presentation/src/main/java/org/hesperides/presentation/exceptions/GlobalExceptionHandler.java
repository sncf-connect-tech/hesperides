package org.hesperides.presentation.exceptions;

import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.hesperides.domain.exceptions.DuplicateException;
import org.hesperides.domain.exceptions.NotFoundException;
import org.hesperides.domain.exceptions.OutOfDateVersionException;
import org.hesperides.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;
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
     * @param ex
     * @return
     */
    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity handleAggregateNotFound(AggregateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getAggregateIdentifier() + ": " + ex.getMessage());
    }

    @ExceptionHandler({DuplicateException.class, OutOfDateVersionException.class})
    public ResponseEntity handleDuplicateTemplateCreation(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RequiredPropertyCannotHaveDefaultValueException.class)
    public ResponseEntity handleAggregateNotFound(RequiredPropertyCannotHaveDefaultValueException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * se produit sur les queries.
     *
     * @param ex
     * @return
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
        return new ResponseEntity<Object>(body != null ? body : ex.getMessage(), headers, status);
    }
}
