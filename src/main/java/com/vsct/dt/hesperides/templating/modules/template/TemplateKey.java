package com.vsct.dt.hesperides.templating.modules.template;

/**
* Created by emeric_martineau on 15/01/2016.
*/
public class TemplateKey {
    private String namespace;
    private String name;

    public TemplateKey(final String namespace, final String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public TemplateKey(final Template template) {
        this(template.getNamespace(), template.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateKey)) return false;

        TemplateKey key = (TemplateKey) o;

        if (name != null ? !name.equals(key.name) : key.name != null) return false;
        if (namespace != null ? !namespace.equals(key.namespace) : key.namespace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }
}
