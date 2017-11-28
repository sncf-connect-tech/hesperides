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

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Util class to create annotation object.
 * <p>
 * Created by emeric_martineau on 05/11/2015.
 */
public class HesperidesAnnotationConstructor {
    private HesperidesAnnotationConstructor() {
        // Nothing
    }

    /**
     * Create annotation object.
     *
     * @param annotation annotation like "@default" or "default"
     * @param value      value of annotation
     * @return anootation object or null if annotation not found.
     */
    public static HesperidesAnnotation createAnnotationObject(final String annotation, final String value) {
        String classAnnotation;
        HesperidesAnnotation obj = null;

        if (annotation.startsWith("@")) {
            classAnnotation = StringUtils.capitalize(annotation.substring(1).toLowerCase());
        } else {
            classAnnotation = StringUtils.capitalize(annotation.toLowerCase());
        }

        try {
            Class<HesperidesAnnotation> clazz = (Class<HesperidesAnnotation>) Class.forName(
                    String.format("com.vsct.dt.hesperides.templating.models.annotation.Hesperides%sAnnotation",
                            classAnnotation));

            Constructor<HesperidesAnnotation> constructor = clazz.getConstructor(String.class, String.class);

            obj = constructor.newInstance(classAnnotation.toLowerCase(), value);
        } catch (ClassNotFoundException e) {
            // Not good.
        } catch (NoSuchMethodException e) {
            // Not good.
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            // Not good.
        } catch (IllegalAccessException e) {
            // Not good.
        }

        return obj;
    }
}
