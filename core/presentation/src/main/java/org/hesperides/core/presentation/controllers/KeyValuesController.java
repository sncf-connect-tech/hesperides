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
import org.hesperides.core.presentation.io.keyvalues.KeyValueInput;
import org.hesperides.core.presentation.io.keyvalues.KeyValueOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeyValueOutput> getKeyValue(@PathVariable("id") String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping("/{id}")
    public ResponseEntity updateKeyValue(Authentication authentication,
                                         @PathVariable("id") String id,
                                         @Valid @RequestBody KeyValueInput keyValueInput) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
