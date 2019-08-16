package org.hesperides.core.domain.templatecontainers.entities;

import com.github.mustachejava.MustacheException;
import lombok.Value;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.templatecontainers.exceptions.InvalidTemplateException;
import org.hesperides.core.domain.templatecontainers.exceptions.PropertyWithSameNameAsIterablePropertyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class Template {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;
    Long versionId;
    TemplateContainer.Key templateContainerKey;

    public Template validateProperties() {
        List<String> iterablePropertiesNames = extractProperties().stream()
                .filter(IterableProperty.class::isInstance)
                .map(IterableProperty.class::cast)
                .map(AbstractProperty::getName)
                .collect(Collectors.toList());
        List<String> propertiesNames = extractProperties().stream()
                .filter(Property.class::isInstance)
                .map(Property.class::cast)
                .map(AbstractProperty::getName)
                .collect(Collectors.toList());
        for (String iterablePropertyName : iterablePropertiesNames) {
            if (propertiesNames.contains(iterablePropertyName)) {
                throw new PropertyWithSameNameAsIterablePropertyException(templateContainerKey.toString(), filename, iterablePropertyName);
            }
        }

        return this;
    }

    public List<AbstractProperty> extractProperties() {
        List<AbstractProperty> properties = new ArrayList<>();
        properties.addAll(extractPropertiesFromStringContent(filename, "filename", filename));
        properties.addAll(extractPropertiesFromStringContent(filename, "location", location));
        properties.addAll(extractPropertiesFromStringContent(filename, "content", content));
        return properties;
    }

    private List<AbstractProperty> extractPropertiesFromStringContent(String fileName, String fieldName, String string) {
        try {
            return AbstractProperty.extractPropertiesFromStringContent(string);
        } catch (IllegalArgumentException | MustacheException illegalArgException) {
            throw new InvalidTemplateException(templateContainerKey.toString(), fileName, fieldName, illegalArgException);
        }
    }

    public Template validateNameNotTaken(Map<String, Template> templates, TemplateContainer.Key key) {
        if (templates.containsKey(name)) {
            throw new DuplicateTemplateCreationException(key, name);
        }
        return this;
    }

    public Template initializeVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                1L,
                templateContainerKey
        );
    }

    public Template validateExistingName(Map<String, Template> templates, TemplateContainer.Key key) {
        if (!templates.containsKey(name)) {
            throw new TemplateNotFoundException(key, name);
        }
        return this;
    }

    public Template validateVersionId(Long expectedVersionId) {
        if (!versionId.equals(expectedVersionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
        }
        return this;
    }

    public Template incrementVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                versionId + 1,
                templateContainerKey
        );
    }

    @Value
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;
    }

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
