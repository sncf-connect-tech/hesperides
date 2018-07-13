package org.hesperides.tests.bdd.templatecontainers;

import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;

import static org.junit.Assert.assertEquals;

public class PropertyAssertions {

    public static void assertProperty(PropertyOutput expectedProperty, PropertyOutput actualProperty) {
        assertEquals(expectedProperty.getName(), actualProperty.getName());
        assertEquals(expectedProperty.isRequired(), actualProperty.isRequired());
        assertEquals(expectedProperty.getComment(), actualProperty.getComment());
        assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        assertEquals(expectedProperty.getPattern(), actualProperty.getPattern());
        assertEquals(expectedProperty.isPassword(), actualProperty.isPassword());
    }
}
