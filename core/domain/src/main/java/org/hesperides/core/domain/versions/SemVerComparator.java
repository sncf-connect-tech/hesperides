package org.hesperides.core.domain.versions;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.Function;

import static java.lang.Integer.parseInt;

/*
 * Fourni un comparateur de Strings qui tente de les comparer
 * avec du semantic versioning, mais fallback en comparaison lexicographique
 */
public class SemVerComparator {
    // Inspiré de java.util.Comparator.comparing
    public static <T> Comparator<T> semVerComparing(Function<? super T, String> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable)
                (e1, e2) ->
                    semVerCompare(keyExtractor.apply(e1), keyExtractor.apply(e2));
    }

    public static int semVerCompare(String v1, String v2) {
        StringTokenizer semVer1 = new StringTokenizer(v1, ".");
        StringTokenizer semVer2 = new StringTokenizer(v2, ".");
        while (semVer1.hasMoreTokens() && semVer2.hasMoreTokens()) {
            String partialVersion1 = semVer1.nextToken();
            String partialVersion2 = semVer2.nextToken();
            try {
                int partialIntVersion1 = parseInt(partialVersion1);
                int partialIntVersion2 = parseInt(partialVersion2);
                if (partialIntVersion1 != partialIntVersion2) {
                    return partialIntVersion1 - partialIntVersion2;
                }
            } catch (NumberFormatException numberFormatException) {
                // Fallback: comparaison de String
                if (!partialVersion1.equals(partialVersion2)) {
                    return partialVersion1.compareTo(partialVersion2);
                }
            }
        }
        if (semVer1.hasMoreTokens() && !semVer2.hasMoreTokens()) {
            // La chaîne de versions v1 est plus longue => c'est elle la plus grande
            return 1;
        }
        if (semVer2.hasMoreTokens() && !semVer1.hasMoreTokens()) {
            // La chaîne de versions v2 est plus longue => c'est elle la plus grande
            return -1;
        }
        // semVer1.hasMoreTokens() && semVer2.hasMoreTokens()
        // On a parcouru toute la liste de chaque côté : les versions sont égales
        return 0;
    }
}
