package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Value
public class GlobalPropertyUsageView {
    private boolean inModel;
    private String path;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GlobalPropertyUsageView that = (GlobalPropertyUsageView) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(inModel, that.inModel)
                .append(path, that.path)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(inModel)
                .append(path)
                .toHashCode();
    }
}
