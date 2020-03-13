package org.hesperides.test.bdd.commons;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.datatable.DataTableType;
import org.hesperides.core.presentation.io.platforms.properties.PlatformDetailedPropertiesOutput.DetailedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.platforms.scenarios.GetDetailedProperties;
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
                GetDetailedProperties.ModuleDetailedProperty.class,
                (Map<String, String> row) -> new GetDetailedProperties.ModuleDetailedProperty(
                        row.get("name"),
                        row.get("storedValue"),
                        row.get("finalValue"),
                        row.get("defaultValue"),
                        Boolean.parseBoolean(row.get("isRequired")),
                        Boolean.parseBoolean(row.get("isPassword")),
                        row.get("pattern"),
                        row.get("comment"),
                        row.get("referencedGlobalProperties"),
                        Boolean.parseBoolean(row.get("isUnused")))
        ));

        typeRegistry.defineDataTableType(new DataTableType(
                DetailedPropertyOutput.class,
                (Map<String, String> row) -> new DetailedPropertyOutput(
                        row.get("name"),
                        row.get("storedValue"),
                        row.get("finalValue"))));

        typeRegistry.defineDataTableType(new DataTableType(
                SaveProperties.IterableProperty.class,
                (Map<String, String> row) -> new SaveProperties.IterableProperty(row.get("iterable"), row.get("bloc"), row.get("name"), row.get("value"))
        ));
    }
}
