/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.presentation.controllers;

import org.hesperides.core.application.keyvalues.KeyValueUseCases;
import org.hesperides.core.domain.keyvalues.entities.KeyValue;
import org.hesperides.core.domain.keyvalues.queries.views.KeyValueView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.keyvalues.KeyValueInput;
import org.hesperides.core.presentation.io.keyvalues.KeyValueOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RequestMapping("/keyvalues")
@RestController
public class KeyValuesController {

    private final KeyValueUseCases keyValueUseCases;

    @Autowired
    public KeyValuesController(KeyValueUseCases keyValueUseCases) {
        this.keyValueUseCases = keyValueUseCases;
    }

    @PostMapping
    public ResponseEntity createKeyValue(Authentication authentication,
                                         @Valid @RequestBody KeyValueInput keyValueInput) {
        KeyValue keyValue = keyValueInput.toDomainInstance();
        User user = new User(authentication);
        String id = keyValueUseCases.createKeyValue(keyValue, user);
        URI location = URI.create("/keyvalues/" + id);
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeyValueOutput> getKeyValue(@PathVariable("id") String id) {
        KeyValueView keyValueView = keyValueUseCases.getKeyValue(id);
        KeyValueOutput keyValueOutput = new KeyValueOutput(keyValueView);
        return ResponseEntity.ok(keyValueOutput);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateKeyValue(Authentication authentication,
                                         @PathVariable("id") String id,
                                         @Valid @RequestBody KeyValueInput keyValueInput) {
        KeyValue keyValue = keyValueInput.toDomainInstance();
        User user = new User(authentication);
        keyValueUseCases.updateKeyValue(id, keyValue, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteKeyValue(Authentication authentication,
                                         @PathVariable("id") String id) {
        User user = new User(authentication);
        keyValueUseCases.deleteKeyValue(id, user);
        return ResponseEntity.noContent().build();
    }
}
