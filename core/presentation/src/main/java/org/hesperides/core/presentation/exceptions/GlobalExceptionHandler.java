package org.hesperides.core.presentation.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.modules.exceptions.UpdateReleaseException;
import org.hesperides.core.domain.platforms.exceptions.InexistantPlatformAtTimeException;
import org.hesperides.core.domain.platforms.exceptions.InvalidPropertyValorisationException;
import org.hesperides.core.domain.technos.exception.UndeletableTechnoInUseException;
import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyWithDefaultValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler({DuplicateException.class, OutOfDateVersionException.class, UndeletableTechnoInUseException.class})
    public ResponseEntity handleConflict(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            RequiredPropertyWithDefaultValueException.class,
            UpdateReleaseException.class,
            InvalidPropertyValorisationException.class})
    public ResponseEntity handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity handleUnauthorized(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * se produit sur les queries.
     *
     * @param ex "not found" failure
     * @return entitiy
     */
    @ExceptionHandler({InexistantPlatformAtTimeException.class, NotFoundException.class})
    public ResponseEntity handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Exceptions non gérées
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleUnexpectedException(Exception ex, WebRequest request) {
        logger.error(ex);
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("message", StringUtils.isNotEmpty(ex.getMessage()) ? ex.getMessage() : ex.toString());
        jsonData.put("status", "500");
        jsonData.put("error", "Internal Server Error");
        jsonData.put("path", request.getDescription(false));
        jsonData.put("stack_trace", ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonData);
    }
}
