package org.hesperides.core.presentation.controllers;

import org.hesperides.core.application.workshopproperties.WorkshopPropertyUseCases;
import org.hesperides.presentation.io.WorkshopPropertyInput;
import org.hesperides.presentation.io.WorkshopPropertyOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/workshop/properties")
@RestController
public class WorkshopPropertiesController extends AbstractController {

    private final WorkshopPropertyUseCases workshopPropertyUseCases;

    @Autowired
    public WorkshopPropertiesController(WorkshopPropertyUseCases workshopPropertyUseCases) {
        this.workshopPropertyUseCases = workshopPropertyUseCases;
    }

    @PostMapping
    public ResponseEntity<WorkshopPropertyOutput> createWorkshopProperty(Authentication authentication,
                                                                         @Valid @RequestBody final WorkshopPropertyInput workshopPropertyInput) {

        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{key}")
    public ResponseEntity<WorkshopPropertyOutput> getWorkshopProperty(@PathVariable("key") final String workshopPropertyKey) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping
    public ResponseEntity<WorkshopPropertyOutput> updateWorkshopProperty(Authentication authentication,
                                                                         @Valid @RequestBody final WorkshopPropertyInput workshopPropertyInput) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
