package org.hesperides.core.domain.templatecontainers.entities;

import com.github.mustachejava.Code;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableProperty extends AbstractProperty {

    List<AbstractProperty> properties;

    public IterableProperty(String name, List<AbstractProperty> properties) {
        super(name);
        this.properties = properties;
    }

    /**
     * Méthode récursive permettant d'extraire les propriétés et les propriétés itérables contenues dans une propriété itérable.
     *
     * @param code
     * @return
     */
    public static IterableProperty extractIterablePropertyFromMustacheCode(IterableCode code) {
        String name = code.getName();
        List<AbstractProperty> properties = new ArrayList<>();

        for (Code childCode : code.getCodes()) {
            if (childCode instanceof ValueCode) {
                properties.add(Property.extractProperty(childCode.getName()));
            } else if (childCode instanceof IterableCode) {
                properties.add(extractIterablePropertyFromMustacheCode((IterableCode) childCode));
            }
        }
        return new IterableProperty(name, properties);
    }
}
