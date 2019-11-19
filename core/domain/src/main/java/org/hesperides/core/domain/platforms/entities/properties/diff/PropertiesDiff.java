package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.visitors.IterablePropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Value
public class PropertiesDiff {

    Set<PropertyVisitor> onlyLeft;
    Set<PropertyVisitor> onlyRight;
    Set<AbstractDifferingProperty> common;
    Set<AbstractDifferingProperty> differingProperties;

    public PropertiesDiff(PropertyVisitorsSequence propertiesLeft, PropertyVisitorsSequence propertiesRight, boolean compareStoredValues) {
        // On procède au diff :
        //  - onlyLeft : la propriété n'est pas présente dans la liste de droite.
        //  - onlyRight : la propriété n'est pas présente dans la liste de gauche.
        //  - common : la propriété est présente dans la liste de droite et sa valeur est la même.
        //  - differing : la propriété est présente dans la liste de droite et sa valeur est différente.
        Set<PropertyVisitor> onlyLeft = new HashSet<>();
        Set<PropertyVisitor> onlyRight = new HashSet<>();
        Set<AbstractDifferingProperty> common = new HashSet<>();
        Set<AbstractDifferingProperty> differingProperties = new HashSet<>();

        // On construit une map pour avoir en clé le nom de la propriété et en valeur l'objet AbstractValuedProperty.
        // Cette mécanique nous sert à retrouver (ou non) la propriété dans la liste d'en face grâce à son nom..
        Map<String, PropertyVisitor> propertyVisitorsRightPerName = propertiesRight.stream().collect(toMap(PropertyVisitor::getName, property -> property));
        Set<String> visitedLeftPropertyNames = new HashSet<>();

        for (PropertyVisitor leftProperty : propertiesLeft.getProperties()) {
            PropertyVisitor rightProperty = propertyVisitorsRightPerName.get(leftProperty.getName());
            if (!hasValue(rightProperty, compareStoredValues)) {
                if (hasValue(leftProperty, compareStoredValues)) {
                    onlyLeft.add(leftProperty);
                } else {
                    common.add(buildDifferingPropertyRecursive(leftProperty, rightProperty, compareStoredValues));
                }
            } else if (!hasValue(leftProperty, compareStoredValues)) {
                onlyRight.add(rightProperty);
            } else {
                AbstractDifferingProperty differingProperty = buildDifferingPropertyRecursive(leftProperty, rightProperty, compareStoredValues);
                if (leftProperty.equals(rightProperty, compareStoredValues)) {
                    common.add(differingProperty);
                } else {
                    differingProperties.add(differingProperty);
                }
            }
            visitedLeftPropertyNames.add(leftProperty.getName());
        }

        propertiesRight.getProperties().stream()
                .filter(rightProperty -> !visitedLeftPropertyNames.contains(rightProperty.getName()))
                .forEach(rightProperty -> {
                    if (hasValue(rightProperty, compareStoredValues)) {
                        onlyRight.add(rightProperty);
                    } else {
                        // Cas où la propriété n'a pas de modèle, n'est pas renseignée à gauche et vide à droite
                        common.add(buildDifferingPropertyRecursive(null, rightProperty, compareStoredValues));
                    }
                });

        this.onlyLeft = onlyLeft;
        this.onlyRight = onlyRight;
        this.common = common;
        this.differingProperties = differingProperties;
    }

    private static boolean hasValue(PropertyVisitor propertyVisitor, boolean compareStoredValues) {
        boolean hasValue = false;

        if (propertyVisitor != null) {
            if (propertyVisitor instanceof IterablePropertyVisitor) {
                IterablePropertyVisitor iterablePropertyVisitor = (IterablePropertyVisitor) propertyVisitor;
                hasValue = iterablePropertyVisitor.getItems().size() > 0;
            } else {
                SimplePropertyVisitor simplePropertyVisitor = (SimplePropertyVisitor) propertyVisitor;
                if (compareStoredValues) {
                    hasValue = simplePropertyVisitor.isValued();
                } else {
                    // Permet de tenir compte de la valeur par défaut si elle est fournie
                    hasValue = simplePropertyVisitor.getValueOrDefault().isPresent() &&
                            // Dans le legacy, une valeur par défaut non fournie est vide (mais pas null)
                            StringUtils.isNotEmpty(simplePropertyVisitor.getValueOrDefault().get());
                }
            }
        }
        return hasValue;
    }

    private static AbstractDifferingProperty buildDifferingPropertyRecursive(PropertyVisitor leftProperty, PropertyVisitor rightProperty, boolean compareStoredValues) {
        AbstractDifferingProperty differingProperty;
        if (leftProperty == null) {
            // Cas où la propriété n'a pas de model, n'est pas renseignée à gauche et vide à droite
            differingProperty = new SimpleDifferingProperty(rightProperty.getName(), (SimplePropertyVisitor) leftProperty, (SimplePropertyVisitor) rightProperty);
        } else if (leftProperty instanceof SimplePropertyVisitor) {
            differingProperty = new SimpleDifferingProperty(leftProperty.getName(), (SimplePropertyVisitor) leftProperty, (SimplePropertyVisitor) rightProperty);
        } else {
            List<PropertyVisitorsSequence> iterablePropertyLeftItems = ((IterablePropertyVisitor) leftProperty).getItems();
            List<PropertyVisitorsSequence> iterablePropertyRightItems = ((IterablePropertyVisitor) rightProperty).getItems();
            int maxRange = Math.max(iterablePropertyLeftItems.size(), iterablePropertyRightItems.size());
            List<PropertiesDiff> propertiesDiffList = IntStream.range(0, maxRange).mapToObj(index -> {
                PropertyVisitorsSequence nestedPropertiesLeft = (index >= iterablePropertyLeftItems.size()) ? PropertyVisitorsSequence.empty() : iterablePropertyLeftItems.get(index);
                PropertyVisitorsSequence nestedPropertiesRight = (index >= iterablePropertyRightItems.size()) ? PropertyVisitorsSequence.empty() : iterablePropertyRightItems.get(index);
                return new PropertiesDiff(nestedPropertiesLeft, nestedPropertiesRight, compareStoredValues);
            }).collect(Collectors.toList());
            differingProperty = new IterableDifferingProperty(leftProperty.getName(), propertiesDiffList);
        }
        return differingProperty;
    }
}
