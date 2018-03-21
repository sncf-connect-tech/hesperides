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
package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.springframework.util.StringUtils;

@Value
public class LegacyTemplate {
    String name;
    String namespace;
    String filename;
    String location;
    String content;
    Rights rights;
    @SerializedName("version_id")
    long versionId;

    @Value
    static class Rights {
        Right user;
        Right group;
        Right other;

        @Value
        static class Right {
            Boolean read;
            Boolean write;
            Boolean execute;
        }
    }

    /**
     * Extract module type from namespace
     */
    public Module.Type getModuleTypeFromNamespace() {
        Module.Type moduleType = null;
        if (StringUtils.hasLength(namespace)) {
            String[] namespaceInfos = namespace.split("#");

            int moduleTypeIndex = 3;
            if (namespaceInfos.length >= moduleTypeIndex + 1) {

                String legacyNamespaceModuleType = namespaceInfos[moduleTypeIndex];
                if (Module.Type.workingcopy.name().equalsIgnoreCase(legacyNamespaceModuleType)) {
                    moduleType = Module.Type.workingcopy;
                } else if (Module.Type.release.name().equalsIgnoreCase(legacyNamespaceModuleType)) {
                    moduleType = Module.Type.release;
                } else {
                    throw new RuntimeException("todo");
                }
            }
        }
        return moduleType;
    }

    public Template toDomainTemplate(Module.Key moduleKey) {
        return new Template(
                this.getName(),
                this.getFilename(),
                this.getLocation(),
                this.getContent(),
                new Template.Rights(
                        new Template.FileRights(
                                this.getRights().getUser().getRead(),
                                this.getRights().getUser().getWrite(),
                                this.getRights().getUser().getExecute()
                        ),
                        new Template.FileRights(
                                this.getRights().getGroup().getRead(),
                                this.getRights().getGroup().getWrite(),
                                this.getRights().getGroup().getExecute()
                        ),
                        // Les droits "other" ne sont pas définis dans l'application actuelle
                        new Template.FileRights(null, null, null)
                ),
                moduleKey
        );
    }

    public static LegacyTemplate fromDomainTemplate(Template template, long versionId) {
        return new LegacyTemplate(
                template.getName(),
                template.getModuleKey().getNamespace(),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                new LegacyTemplate.Rights(
                        new LegacyTemplate.Rights.Right(
                                template.getRights().getUser().getRead(),
                                template.getRights().getUser().getWrite(),
                                template.getRights().getUser().getExecute()
                        ),
                        new LegacyTemplate.Rights.Right(
                                template.getRights().getGroup().getRead(),
                                template.getRights().getGroup().getWrite(),
                                template.getRights().getGroup().getExecute()
                        ),
                        // Les droits "other" ne sont pas définis dans l'application actuelle
                        null
                ),
                versionId);
    }
}
