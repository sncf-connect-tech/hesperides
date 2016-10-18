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

package com.vsct.dt.hesperides.indexation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by william_montaz on 05/09/14.
 */
public final class DocWrapper<T> {

    private T doc;

    @JsonProperty(value = "doc")
    public T getDoc() {
        return doc;
    }

    public void setDoc(final T doc) {
        this.doc = doc;
    }

    public static <T> DocWrapper<T> of(final T entity) {
        DocWrapper docWrapper = new DocWrapper();
        docWrapper.setDoc(entity);
        return docWrapper;
    }
}
