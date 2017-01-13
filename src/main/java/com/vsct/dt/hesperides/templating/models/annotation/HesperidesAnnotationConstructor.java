/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.templating.models.annotation;

import com.vsct.dt.hesperides.templating.models.exception.ModelAnnotationException;

public class HesperidesAnnotationConstructor {
    public static HesperidesAnnotation createAnnotationObject(final String annotation, final String propertyName) {
        switch (annotation) {
            case "comment":
                return new HesperidesCommentAnnotation();
            case "default":
                return new HesperidesDefaultAnnotation();
            case "password":
                return new HesperidesPasswordAnnotation();
            case "pattern":
                return new HesperidesPatternAnnotation();
            case "required":
                return new HesperidesRequiredAnnotation();
            default:
                throw new ModelAnnotationException(
                    String.format("Unknown annotation '%s' for property '%s'",
                            annotation, propertyName));
        }
    }
}
