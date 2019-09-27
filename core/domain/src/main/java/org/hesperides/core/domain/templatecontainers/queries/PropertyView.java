package org.hesperides.core.domain.templatecontainers.queries;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;
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
    public Stream<PropertyView> flattenProperties() {
        return Stream.of(this);
    }

    public boolean isRequiredAndNotValorised(List<ValuedProperty> matchingValuedProperties) {
        return isRequired && (CollectionUtils.isEmpty(matchingValuedProperties) || notEveryPropertyHasValue(matchingValuedProperties));
    }

    private boolean notEveryPropertyHasValue(List<ValuedProperty> valuedProperties) {
        return valuedProperties.stream()
                .map(ValuedProperty::getValue)
                .anyMatch(StringUtils::isEmpty);
    }

    public boolean hasValueThatDoesntMatchPattern(List<ValuedProperty> matchingValuedProperties) {
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
