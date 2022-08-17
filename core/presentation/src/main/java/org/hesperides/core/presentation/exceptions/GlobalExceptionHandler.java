package org.hesperides.core.presentation.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.exceptions.OutOfDateException;
import org.hesperides.core.domain.modules.exceptions.ModuleHasWorkingcopyTechnoException;
import org.hesperides.core.domain.modules.exceptions.ModuleUsedByPlatformsException;
import org.hesperides.core.domain.technos.exception.UndeletableTechnoInUseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralisation de la gestion des exceptions (bien pratique)
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    protected void beforeHandling(Exception exception) {
    }

    /**
     * se produit quand on veut executer une commande sur un aggregat qui n'existe pas.
     *
     * @param exception "not found" failure
     * @return entity
     */
    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity handleAggregateNotFound(AggregateNotFoundException exception) {
        beforeHandling(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getAggregateIdentifier() + ": " + exception.getMessage());
    }

    // 409: La requête ne peut être traitée en l’état actuel
    @ExceptionHandler({DuplicateException.class, OutOfDateException.class, UndeletableTechnoInUseException.class,
            ModuleUsedByPlatformsException.class, ModuleHasWorkingcopyTechnoException.class})
    public ResponseEntity handleConflict(Exception exception) {
        beforeHandling(exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity handleBadRequest(Exception exception) {
        beforeHandling(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
    public ResponseEntity handleForbidden(Exception exception) {
        beforeHandling(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    /**
     * se produit sur les queries.
     *
     * @param exception "not found" failure
     * @return entitiy
     */
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity handleNotFound(Exception exception) {
        beforeHandling(exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleUnexpectedException(Exception exception, WebRequest request) {
        beforeHandling(exception);
        String path = request.getDescription(false);
        logger.error("Unexpected error (path=" + path + "):");
        logger.error(exception);
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("message", StringUtils.isNotEmpty(exception.getMessage()) ? exception.getMessage() : exception.toString());
        jsonData.put("status", "500");
        jsonData.put("error", "Internal Server Error");
        jsonData.put("path", path);
        jsonData.put("stacktrace", ExceptionUtils.getStackTrace(exception));
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonData);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        beforeHandling(exception);
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body != null ? body : exception.getMessage(), headers, status);
    }
}
