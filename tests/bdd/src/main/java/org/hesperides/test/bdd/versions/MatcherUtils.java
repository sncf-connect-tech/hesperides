package org.hesperides.test.bdd.versions;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static org.hesperides.core.domain.versions.SemVerComparator.semVerCompare;

public class MatcherUtils {

    public static TypeSafeMatcher<String> semVerGreaterThan(String v2) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String v1) {
                return semVerCompare(v1, v2) > 0;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("greater than ").appendValue(v2);
            }
        };
    }
}
