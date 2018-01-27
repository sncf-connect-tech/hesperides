package org.hesperides.domain.modules;

/**
 * type de module possible.
 */
public enum ModuleType {
    workingcopy("wc"),
    release("release");

    private final String minimizedForm;

    ModuleType(String minimizedForm) {
        this.minimizedForm = minimizedForm;
    }

    public String getMinimizedForm() {
        return minimizedForm;
    }
}
