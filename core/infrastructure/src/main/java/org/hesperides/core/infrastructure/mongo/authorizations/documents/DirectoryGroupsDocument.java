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
package org.hesperides.core.infrastructure.mongo.authorizations.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryGroupsDocument {
    private String key;
    private List<String> values;

    public static List<DirectoryGroupsDocument> fromMapOfList(Map<String, List<String>> directoryGroups) {
        List<DirectoryGroupsDocument> directoryGroupsDocuments = new ArrayList<>();
        if (directoryGroups != null) {
            directoryGroupsDocuments = directoryGroups.entrySet().stream()
                    .map(entry -> new DirectoryGroupsDocument(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
        return directoryGroupsDocuments;
    }

    public static Map<String, List<String>> toMapOfList(List<DirectoryGroupsDocument> directoryGroupsDocuments) {
        Map<String, List<String>> directoryGroups = new HashMap<>();
        if (directoryGroupsDocuments != null) {
            directoryGroups = directoryGroupsDocuments.stream().collect(Collectors.toMap(DirectoryGroupsDocument::getKey,
                    DirectoryGroupsDocument::getValues, (p1, p2) -> p1));
        }
        return directoryGroups;
    }
}
