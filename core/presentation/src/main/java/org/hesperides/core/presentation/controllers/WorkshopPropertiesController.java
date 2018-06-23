package org.hesperides.presentation.controllers;

import org.hesperides.application.workshopproperties.WorkshopPropertyUseCases;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.hesperides.presentation.io.WorkshopPropertyInput;
import org.hesperides.presentation.io.WorkshopPropertyOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.hesperides.domain.security.User.fromAuthentication;

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

        WorkshopProperty workshopProperty = workshopPropertyInput.toDomainInstance();
        String createdWorkshopPropertyKey = workshopPropertyUseCases.createWorkshopProperty(workshopProperty, fromAuthentication(authentication));

        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopProperty(createdWorkshopPropertyKey);
        WorkshopPropertyOutput workshopPropertyOutput = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);

        return ResponseEntity.ok(workshopPropertyOutput);
    }

    @GetMapping("/{key}")
    public ResponseEntity<WorkshopPropertyOutput> getWorkshopProperty(@PathVariable("key") final String workshopPropertyKey) {
        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopProperty(workshopPropertyKey);
        WorkshopPropertyOutput workshopPropertyOutput = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);
        return ResponseEntity.ok(workshopPropertyOutput);
    }

    @PutMapping
    public ResponseEntity<WorkshopPropertyOutput> updateWorkshopProperty(Authentication authentication,
                                                                         @Valid @RequestBody final WorkshopPropertyInput workshopPropertyInput) {

        WorkshopProperty workshopProperty = workshopPropertyInput.toDomainInstance();
        workshopPropertyUseCases.updateWorkshopProperty(workshopProperty, fromAuthentication(authentication));

        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopProperty(workshopPropertyInput.getKey());
        WorkshopPropertyOutput workshopPropertyOutput = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);

        return ResponseEntity.ok(workshopPropertyOutput);
    }
}
