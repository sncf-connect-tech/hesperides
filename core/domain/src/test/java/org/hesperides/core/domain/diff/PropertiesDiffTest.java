package org.hesperides.core.domain.diff;

import org.hesperides.core.domain.platforms.entities.properties.diff.AbstractDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.visitors.IterablePropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class PropertiesDiffTest {

    @Test
    public void testDiffValuedProperties() {
        // JDD
        PropertyVisitor p1pty1 = makeSimplePropertyVisitor("property-X-in-p1-and-p2", "a-value");
        PropertyVisitor p1pty2 = makeSimplePropertyVisitor("property-Y-in-p1-and-p2", "another-value");
        PropertyVisitor p1pty3 = makeSimplePropertyVisitor("property-only-in-p1", "p1-value");
        PropertyVisitor p2pty1 = makeSimplePropertyVisitor("property-X-in-p1-and-p2", "a-differing-value");
        PropertyVisitor p2pty2 = makeSimplePropertyVisitor("property-Y-in-p1-and-p2", "another-value");
        PropertyVisitor p2pty3 = makeSimplePropertyVisitor("property-only-in-p2", "p2-value");

        // Test
        PropertiesDiff pdiff = PropertiesDiff.performDiff(
                new PropertyVisitorsSequence(Arrays.asList(p1pty1, p1pty2, p1pty3)),
                new PropertyVisitorsSequence(Arrays.asList(p2pty1, p2pty2, p2pty3)),
                false);
        assertEquals(1, pdiff.getOnlyLeft().size());
        assertEquals(1, pdiff.getOnlyRight().size());
        assertEquals(1, pdiff.getCommon().size());
        assertEquals(1, pdiff.getDifferingProperties().size());

        List<String> onlyLeftPropertiesName = pdiff.getOnlyLeft().stream().map(PropertyVisitor::getName).collect(Collectors.toList());
        List<String> onlyLeftPropertiesNameExpected = singletonList("property-only-in-p1");
        assertEquals(onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

        List<String> onlyRightPropertiesName = pdiff.getOnlyRight().stream().map(PropertyVisitor::getName).collect(Collectors.toList());
        List<String> onlyRightPropertiesNameExpected = singletonList("property-only-in-p2");
        assertEquals(onlyRightPropertiesNameExpected, onlyRightPropertiesName);

        List<String> commonPropertiesName = pdiff.getCommon().stream().map(AbstractDifferingProperty::getName).collect(Collectors.toList());
        List<String> commonPropertiesNameExpected = singletonList("property-Y-in-p1-and-p2");
        assertEquals(commonPropertiesNameExpected, commonPropertiesName);

        List<String> differingPropertiesName = pdiff.getDifferingProperties().stream().map(AbstractDifferingProperty::getName).collect(Collectors.toList());
        List<String> differingPropertiesNameExpected = singletonList("property-X-in-p1-and-p2");
        assertEquals(differingPropertiesNameExpected, differingPropertiesName);
    }

    @Test
    public void testDiffIterableValuedProperties_commonItem() {
        PropertyVisitor platformOneBlocOneNestedPptyOne = makeSimplePropertyVisitor("nested-simple-property-one", "one");
        PropertyVisitor platformOneBlocOneNestedPptyTwo = makeSimplePropertyVisitor("nested-simple-property-two", "two");
        PropertyVisitor platformOneBlocOneNestedPptyThree = makeSimplePropertyVisitor("nested-simple-property-three", "three");
        PropertyVisitorsSequence platformOneIterablePropertyItem = new PropertyVisitorsSequence(Arrays.asList(platformOneBlocOneNestedPptyOne, platformOneBlocOneNestedPptyTwo, platformOneBlocOneNestedPptyThree));
        IterablePropertyVisitor platformOneIterablePpty = new IterablePropertyVisitor("iterable-property-X-in-p1-and-p2", singletonList(platformOneIterablePropertyItem));

        PropertyVisitor platformTwoBlocOneNestedPptyOne = makeSimplePropertyVisitor("nested-simple-property-one", "one");
        PropertyVisitor platformTwoBlocOneNestedPptyTwo = makeSimplePropertyVisitor("nested-simple-property-two", "two");
        PropertyVisitor platformTwoBlocOneNestedPptyThree = makeSimplePropertyVisitor("nested-simple-property-three", "three");
        PropertyVisitorsSequence platformTwoIterablePropertyItem = new PropertyVisitorsSequence(Arrays.asList(platformTwoBlocOneNestedPptyOne, platformTwoBlocOneNestedPptyTwo, platformTwoBlocOneNestedPptyThree));
        IterablePropertyVisitor platformTwoIterablePpty = new IterablePropertyVisitor("iterable-property-X-in-p1-and-p2", singletonList(platformTwoIterablePropertyItem));

        // Test
        PropertiesDiff pdiff = PropertiesDiff.performDiff(
                new PropertyVisitorsSequence(singletonList(platformOneIterablePpty)),
                new PropertyVisitorsSequence(singletonList(platformTwoIterablePpty)),
                false);
        assertEquals(1, pdiff.getCommon().size());
    }

    @Test
    public void testDiffIterableValuedProperties_differingItem() {
        PropertyVisitor platformOneBlocOneNestedPptyOne = makeSimplePropertyVisitor("nested-simple-property-one", "one");
        PropertyVisitor platformOneBlocOneNestedPptyTwo = makeSimplePropertyVisitor("nested-simple-property-two", "two");
        PropertyVisitor platformOneBlocOneNestedPptyThree = makeSimplePropertyVisitor("nested-simple-property-three", "three");
        PropertyVisitorsSequence platformOneIterablePropertyItem = new PropertyVisitorsSequence(Arrays.asList(platformOneBlocOneNestedPptyOne, platformOneBlocOneNestedPptyTwo, platformOneBlocOneNestedPptyThree));

        PropertyVisitor platformOneBloc2OneNestedPptyOne = makeSimplePropertyVisitor("nested-simple-property-one", "toto");
        PropertyVisitor platformOneBloc2OneNestedPptyTwo = makeSimplePropertyVisitor("nested-simple-property-two", "titi");
        PropertyVisitor platformOneBloc2OneNestedPptyThree = makeSimplePropertyVisitor("nested-simple-property-three", "tata");
        PropertyVisitorsSequence platformOneIterablePropertyItem2 = new PropertyVisitorsSequence(Arrays.asList(platformOneBloc2OneNestedPptyOne, platformOneBloc2OneNestedPptyTwo, platformOneBloc2OneNestedPptyThree));

        IterablePropertyVisitor platformOneIterablePpty = new IterablePropertyVisitor("iterable-property-X-in-p1-and-p2", Arrays.asList(platformOneIterablePropertyItem, platformOneIterablePropertyItem2));

        PropertyVisitor platformTwoBlocOneNestedPptyOne = makeSimplePropertyVisitor("nested-simple-property-one", "one");
        PropertyVisitor platformTwoBlocOneNestedPptyTwo = makeSimplePropertyVisitor("nested-simple-property-two", "two");
        PropertyVisitor platformTwoBlocOneNestedPptyThree = makeSimplePropertyVisitor("nested-simple-property-three", "three");
        PropertyVisitorsSequence platformTwoIterablePropertyItem = new PropertyVisitorsSequence(Arrays.asList(platformTwoBlocOneNestedPptyOne, platformTwoBlocOneNestedPptyTwo, platformTwoBlocOneNestedPptyThree));

        IterablePropertyVisitor platformTwoIterablePpty = new IterablePropertyVisitor("iterable-property-X-in-p1-and-p2", singletonList(platformTwoIterablePropertyItem));

        // Test
        PropertiesDiff pdiff = PropertiesDiff.performDiff(
                new PropertyVisitorsSequence(singletonList(platformOneIterablePpty)),
                new PropertyVisitorsSequence(singletonList(platformTwoIterablePpty)),
                false);
        assertEquals(1, pdiff.getDifferingProperties().size());

    }

    private SimplePropertyVisitor makeSimplePropertyVisitor(String name, String value) {
        return new SimplePropertyVisitor(
                singletonList(new PropertyView(name, "{{" + name + "}}", false, null, null, null, false)),
                new ValuedPropertyView(name, value)
        );
    }

}
