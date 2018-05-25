package org.hesperides.domain.framework;

import org.axonframework.queryhandling.responsetypes.AbstractResponseType;
import org.axonframework.queryhandling.responsetypes.ResponseType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * response type. cette classe devrait aller dans Axon.
 *
 * @param <R>
 */
public class OptionalResponseType<R> extends AbstractResponseType<Optional<R>> {
    private OptionalResponseType(Class<?> expectedResponseType) {
        super(expectedResponseType);
    }

    /**
     * Specify the desire to retrieve an optional of instance of type {@code R} when performing a query.
     *
     * @param type the {@code R} which is expected to be the response type
     * @param <R>  the generic type of the instantiated
     *             {@link org.axonframework.queryhandling.responsetypes.ResponseType}
     * @return a {@link org.axonframework.queryhandling.responsetypes.ResponseType} specifying the desire to retrieve an
     * optional of instances of type {@code R}
     */
    public static <R> ResponseType<Optional<R>> optionalInstancesOf(Class<R> type) {
        return new OptionalResponseType<>(type);
    }

    @Override
    public boolean matches(Type responseType) {
        return isParameterizedType(responseType) &&
                isParameterizedTypeOfExpectedType(responseType) &&
                isOptional(responseType);
    }

    @SuppressWarnings("unchecked")
    private boolean isOptional(Type responseType) {
        return ((Class) ((ParameterizedType) responseType).getRawType()).isAssignableFrom(Optional.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<R> convert(Object response) {
        if (response instanceof Optional) {
            return super.convert(response);
        } else {
            return Optional.ofNullable((R) response);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Optional<R>> responseMessagePayloadType() {
        return (Class<Optional<R>>) expectedResponseType;
    }
}
