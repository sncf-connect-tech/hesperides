package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
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

    public static PropertiesDiff performDiff(PropertyVisitorsSequence propertiesLeft, PropertyVisitorsSequence propertiesRight, boolean compareStoredValues) {
        // On procède au diff :
        //  - onlyLeft : la propriété n'est pas présente dans la liste de droite.
        //  - onlyRight : la propriété n'est pas présente dans la liste de gauche.
        //  - common : la propriété est présente dans la liste de droite et sa valeur est la même.
        //  - differing : la propriété est présente dans la liste de droite et sa valeur est différente.
        Set<PropertyVisitor> onlyLeft = new HashSet<>();
        Set<PropertyVisitor> onlyRight = new HashSet<>();
        Set<AbstractDifferingProperty> common = new HashSet<>();
        Set<AbstractDifferingProperty> differing = new HashSet<>();

        // On construit une map pour avoir en clé le nom de la propriété et en valeur l'objet AbstractValuedProperty.
        // Cette mécanique nous sert à retrouver (ou non) la propriété dans la liste d'en face grâce à son nom..
        Map<String, PropertyVisitor> propertyVisitorsRightPerName = propertiesRight.stream().collect(toMap(PropertyVisitor::getName, property -> property));

        for (PropertyVisitor leftProperty : propertiesLeft.getProperties()) {
            PropertyVisitor rightProperty = propertyVisitorsRightPerName.get(leftProperty.getName());
            if (!isValued(rightProperty)) {
                if (isValued(leftProperty)) {
                    onlyLeft.add(leftProperty);
                } else {
                    common.add(buildDifferingPropertyRecursive(leftProperty, rightProperty, compareStoredValues));
                }
            } else if (!isValued(leftProperty)) {
                onlyRight.add(rightProperty);
            } else {
                AbstractDifferingProperty differingProperty = buildDifferingPropertyRecursive(leftProperty, rightProperty, compareStoredValues);
                if (leftProperty.equals(rightProperty, compareStoredValues)) {
                    common.add(differingProperty);
                } else {
                    differing.add(differingProperty);
                }
            }
        }

        return new PropertiesDiff(onlyLeft, onlyRight, common, differing);
    }

    private static boolean isValued(PropertyVisitor propertyVisitor) {
        return propertyVisitor != null && (propertyVisitor instanceof IterablePropertyVisitor || ((SimplePropertyVisitor) propertyVisitor).isValued());
    }

    private static AbstractDifferingProperty buildDifferingPropertyRecursive(PropertyVisitor leftProperty, PropertyVisitor rightProperty, boolean compareStoredValues) {
        AbstractDifferingProperty differingProperty;
        if (leftProperty instanceof SimplePropertyVisitor) {
            differingProperty = new SimpleDifferingProperty(leftProperty.getName(), (SimplePropertyVisitor) leftProperty, (SimplePropertyVisitor) rightProperty);
        } else {
            List<PropertyVisitorsSequence> iterablePropertyLeftItems = ((IterablePropertyVisitor) leftProperty).getItems();
            List<PropertyVisitorsSequence> iterablePropertyRightItems = ((IterablePropertyVisitor) rightProperty).getItems();
            int maxRange = Math.max(iterablePropertyLeftItems.size(), iterablePropertyRightItems.size());
            List<PropertiesDiff> propertiesDiffList = IntStream.range(0, maxRange).mapToObj(index -> {
                PropertyVisitorsSequence nestedPropertiesLeft = (index >= iterablePropertyLeftItems.size()) ? PropertyVisitorsSequence.empty() : iterablePropertyLeftItems.get(index);
                PropertyVisitorsSequence nestedPropertiesRight = (index >= iterablePropertyRightItems.size()) ? PropertyVisitorsSequence.empty() : iterablePropertyRightItems.get(index);
                return performDiff(nestedPropertiesLeft, nestedPropertiesRight, compareStoredValues);
            }).collect(Collectors.toList());
            differingProperty = new IterableDifferingProperty(leftProperty.getName(), propertiesDiffList);
        }
        return differingProperty;
    }
}
