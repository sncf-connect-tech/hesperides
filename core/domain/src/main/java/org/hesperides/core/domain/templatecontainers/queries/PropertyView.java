package org.hesperides.core.domain.templatecontainers.queries;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.PropertyPatternNotMatchedException;
import org.hesperides.core.domain.platforms.exceptions.RequiredPropertyNotValorisedException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class PropertyView extends AbstractPropertyView {

    String mustacheContent;
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;

    public PropertyView(String name, String mustacheContent, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword) {
        super(name);
        this.mustacheContent = mustacheContent;
        this.isRequired = isRequired;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
        this.isPassword = isPassword;
    }

    @Override
    protected Stream<PropertyView> flattenProperties() {
        return Stream.of(this);
    }

    public void validateRequiredAndPatternProperties(List<ValuedProperty> valuedProperties) {
        List<ValuedProperty> matchingValuedProperties = valuedProperties.stream()
                .filter(valuedPropery -> StringUtils.equals(valuedPropery.getName(), getName()))
                .collect(Collectors.toList());

        if (isRequiredAndNotValorised(matchingValuedProperties)) {
            throw new RequiredPropertyNotValorisedException(getName());
        } else if (hasValueThatDoesntMatchPattern(matchingValuedProperties)) {
            throw new PropertyPatternNotMatchedException(getName(), pattern);
        }
    }

    private boolean isRequiredAndNotValorised(List<ValuedProperty> valuedProperties) {
        return isRequired && hasNotAnyValue(valuedProperties);
    }

    private boolean hasNotAnyValue(List<ValuedProperty> valuedProperties) {
        return CollectionUtils.isEmpty(valuedProperties) || valuedProperties.stream()
                .map(ValuedProperty::getValue)
                .allMatch(StringUtils::isEmpty);
    }

    private boolean hasValueThatDoesntMatchPattern(List<ValuedProperty> matchingValuedProperties) {
        return StringUtils.isNotEmpty(pattern) && anyFilledPropertyDoesntMatchPattern(matchingValuedProperties, pattern);
    }

    private boolean anyFilledPropertyDoesntMatchPattern(List<ValuedProperty> valuedProperties, String stringPattern) {
        Pattern pattern = Pattern.compile(stringPattern);
        return valuedProperties.stream()
                .map(ValuedProperty::getValue)
                .filter(StringUtils::isNotEmpty)
                .anyMatch(value -> !pattern.matcher(value).matches());
    }
}
