package org.hesperides.test.bdd;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.datatable.DataTableType;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.platforms.scenarios.GetPropertiesWithDetails;
import org.hesperides.test.bdd.platforms.scenarios.SaveProperties;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
                ValuedPropertyIO.class,
                (Map<String, String> row) -> new ValuedPropertyIO(row.get("name"), row.get("value"))
        ));
        typeRegistry.defineDataTableType(new DataTableType(
                GetPropertiesWithDetails.TestPropertyWithDetails.class,
                (Map<String, String> row) -> new GetPropertiesWithDetails.TestPropertyWithDetails(row.get("name"), row.get("storedValue"), row.get("finalValue"), row.get("defaultValue"), row.get("transformations"))
        ));
        typeRegistry.defineDataTableType(new DataTableType(
                SaveProperties.IterableProperty.class,
                (Map<String, String> row) -> new SaveProperties.IterableProperty(row.get("iterable"), row.get("bloc"), row.get("name"), row.get("value"))
        ));
    }
}
