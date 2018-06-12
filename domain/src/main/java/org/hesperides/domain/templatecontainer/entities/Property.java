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
package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;

@Value
public class Property {
    String name;
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;

    public enum Option {
        IS_REQUIRED("required"),
        COMMENT("comment"),
        DEFAULT_VALUE("default"),
        PATTERN("pattern"),
        IS_PASSWORD("password");

        private final String name;

        Option(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Option fromName(String name) {
            Option result = null;
            for (Option option : Option.values()) {
                if (option.getName().equalsIgnoreCase(name)) {
                    result = option;
                    break;
                }
            }
            return result;
        }
    }
}
