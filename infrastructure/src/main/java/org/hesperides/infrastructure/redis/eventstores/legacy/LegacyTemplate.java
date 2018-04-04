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

    public static LegacyTemplate fromDomainTemplate(Template template) {
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
                template.getVersionId());
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
                name,
                filename,
                location,
                content,
                this.rights != null ? this.rights.toDomain() : new Template.Rights(new Template.FileRights(), new Template.FileRights(), new Template.FileRights()),
                versionId,
                moduleKey
        );
    }

    @Value
    static class Rights {
        Right user;
        Right group;
        Right other;

        Template.Rights toDomain() {
            Template.FileRights uRights = user != null ? user.toDomain() : new Template.FileRights();
            Template.FileRights gRights = group != null ? group.toDomain() : new Template.FileRights();
            // Les droits "other" ne sont pas définis dans l'application actuelle
            return new Template.Rights(uRights, gRights, new Template.FileRights());
        }

        @Value
        static class Right {
            Boolean read;
            Boolean write;
            Boolean execute;

            Template.FileRights toDomain() {
                return new Template.FileRights(read, write, execute);
            }
        }
    }
}
