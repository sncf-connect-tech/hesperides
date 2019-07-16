package org.hesperides.core.domain.diff;

import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.AbstractDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DiffTest {

    @Test
    public void testDiffValuedProperties() {
        // JDD
        ValuedProperty p1pty1 = new ValuedProperty("property-X-in-p1-and-p2", "a-value");
        ValuedProperty p1pty2 = new ValuedProperty("property-Y-in-p1-and-p2", "another-value");
        ValuedProperty p1pty3 = new ValuedProperty("property-only-in-p1", "p1-value");
        ValuedProperty p2pty1 = new ValuedProperty("property-X-in-p1-and-p2", "a-differing-value");
        ValuedProperty p2pty2 = new ValuedProperty("property-Y-in-p1-and-p2", "another-value");
        ValuedProperty p2pty3 = new ValuedProperty("property-only-in-p2", "p2-value");

        // Test
        PropertiesDiff pdiff = AbstractValuedProperty.diff(Arrays.asList(p1pty1, p1pty2, p1pty3), Arrays.asList(p2pty1, p2pty2, p2pty3));
        assertEquals(1, pdiff.getOnlyLeft().size());
        assertEquals(1, pdiff.getOnlyRight().size());
        assertEquals(1, pdiff.getCommon().size());
        assertEquals(1, pdiff.getDifferingProperties().size());

        List<String> onlyLeftPropertiesName = pdiff.getOnlyLeft().stream().map(AbstractValuedProperty::getName).collect(Collectors.toList());
        List<String> onlyLeftPropertiesNameExpected = Collections.singletonList("property-only-in-p1");
        assertEquals(onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

        List<String> onlyRightPropertiesName = pdiff.getOnlyRight().stream().map(AbstractValuedProperty::getName).collect(Collectors.toList());
        List<String> onlyRightPropertiesNameExpected = Collections.singletonList("property-only-in-p2");
        assertEquals(onlyRightPropertiesNameExpected, onlyRightPropertiesName);

        List<String> commonPropertiesName = pdiff.getCommon().stream().map(AbstractValuedProperty::getName).collect(Collectors.toList());
        List<String> commonPropertiesNameExpected = Collections.singletonList("property-Y-in-p1-and-p2");
        assertEquals(commonPropertiesNameExpected, commonPropertiesName);

        List<String> differingPropertiesName = pdiff.getDifferingProperties().stream().map(AbstractDifferingProperty::getName).collect(Collectors.toList());
        List<String> differingPropertiesNameExpected = Collections.singletonList("property-X-in-p1-and-p2");
        assertEquals(differingPropertiesNameExpected, differingPropertiesName);
    }

    @Test
    public void testDiffIterableValuedProperties_commonItem() {
        AbstractValuedProperty platformOneBlocOneNestedPptyOne = new ValuedProperty("nested-simple-property-one", "one");
        AbstractValuedProperty platformOneBlocOneNestedPptyTwo = new ValuedProperty("nested-simple-property-two", "two");
        AbstractValuedProperty platformOneBlocOneNestedPptyThree = new ValuedProperty("nested-simple-property-three", "three");
        IterablePropertyItem platformOneIterablePropertyItem = new IterablePropertyItem("title", Arrays.asList(platformOneBlocOneNestedPptyOne, platformOneBlocOneNestedPptyTwo, platformOneBlocOneNestedPptyThree));
        IterableValuedProperty platformOneIterablePpty = new IterableValuedProperty("iterable-property-X-in-p1-and-p2", Collections.singletonList(platformOneIterablePropertyItem));


        AbstractValuedProperty platformTwoBlocOneNestedPptyOne = new ValuedProperty("nested-simple-property-one", "one");
        AbstractValuedProperty platformTwoBlocOneNestedPptyTwo = new ValuedProperty("nested-simple-property-two", "two");
        AbstractValuedProperty platformTwoBlocOneNestedPptyThree = new ValuedProperty("nested-simple-property-three", "three");
        IterablePropertyItem platformTwoIterablePropertyItem = new IterablePropertyItem("title", Arrays.asList(platformTwoBlocOneNestedPptyOne, platformTwoBlocOneNestedPptyTwo, platformTwoBlocOneNestedPptyThree));
        IterableValuedProperty platformTwoIterablePpty = new IterableValuedProperty("iterable-property-X-in-p1-and-p2", Collections.singletonList(platformTwoIterablePropertyItem));

        // Test
        PropertiesDiff pdiff = AbstractValuedProperty.diff(Collections.singletonList(platformOneIterablePpty), Collections.singletonList(platformTwoIterablePpty));
        assertEquals(1, pdiff.getCommon().size());
    }

    @Test
    public void testDiffIterableValuedProperties_differingItem() {
        AbstractValuedProperty platformOneBlocOneNestedPptyOne = new ValuedProperty("nested-simple-property-one", "one");
        AbstractValuedProperty platformOneBlocOneNestedPptyTwo = new ValuedProperty("nested-simple-property-two", "two");
        AbstractValuedProperty platformOneBlocOneNestedPptyThree = new ValuedProperty("nested-simple-property-three", "three");
        IterablePropertyItem platformOneIterablePropertyItem = new IterablePropertyItem("title", Arrays.asList(platformOneBlocOneNestedPptyOne, platformOneBlocOneNestedPptyTwo, platformOneBlocOneNestedPptyThree));

        AbstractValuedProperty platformOneBloc2OneNestedPptyOne = new ValuedProperty("nested-simple-property-one", "toto");
        AbstractValuedProperty platformOneBloc2OneNestedPptyTwo = new ValuedProperty("nested-simple-property-two", "titi");
        AbstractValuedProperty platformOneBloc2OneNestedPptyThree = new ValuedProperty("nested-simple-property-three", "tata");
        IterablePropertyItem platformOneIterablePropertyItem2 = new IterablePropertyItem("title", Arrays.asList(platformOneBloc2OneNestedPptyOne, platformOneBloc2OneNestedPptyTwo, platformOneBloc2OneNestedPptyThree));

        IterableValuedProperty platformOneIterablePpty = new IterableValuedProperty("iterable-property-X-in-p1-and-p2", Arrays.asList(platformOneIterablePropertyItem, platformOneIterablePropertyItem2));

        AbstractValuedProperty platformTwoBlocOneNestedPptyOne = new ValuedProperty("nested-simple-property-one", "one");
        AbstractValuedProperty platformTwoBlocOneNestedPptyTwo = new ValuedProperty("nested-simple-property-two", "two");
        AbstractValuedProperty platformTwoBlocOneNestedPptyThree = new ValuedProperty("nested-simple-property-three", "three");
        IterablePropertyItem platformTwoIterablePropertyItem = new IterablePropertyItem("title", Arrays.asList(platformTwoBlocOneNestedPptyOne, platformTwoBlocOneNestedPptyTwo, platformTwoBlocOneNestedPptyThree));

        IterableValuedProperty platformTwoIterablePpty = new IterableValuedProperty("iterable-property-X-in-p1-and-p2", Collections.singletonList(platformTwoIterablePropertyItem));


        // Test
        PropertiesDiff pdiff = AbstractValuedProperty.diff(Arrays.asList(platformOneIterablePpty), Arrays.asList(platformTwoIterablePpty));
        assertEquals(1, pdiff.getDifferingProperties().size());

    }
}
