package org.hesperides.test.bdd.commons;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

public final class DataTableHelper {

    // À partir de la version 5 de Cucumber, les cellules vides des `DataTable`
    // sont remplacées par la valeur `null`, ce qui n'était pas le cas avant.
    // Elles étaient considérées comme des chaînes de caractères vides (`""`).
    // Cette méthode utilitaire permet de ne pas avoir à modifier toutes les
    // features impactées par cette évolution.
    public static String decodeValue(String value) {
        String result;
        if (value == null) {
            result = EMPTY;
        } else if (value.contains("<space>")) {
            result = value.replace("<space>", SPACE);
        } else if ("<null>".equals(value)) {
            result = null;
        } else {
            result = value;
        }
        return result;
    }
}
