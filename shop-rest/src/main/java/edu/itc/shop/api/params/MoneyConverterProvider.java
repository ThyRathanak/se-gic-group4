package edu.itc.shop.api.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import edu.itc.shop.domain.Money;

@Provider
public class MoneyConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(
            Class<T> rawType,
            Type genericType,
            Annotation[] annotations) {

        if (rawType != Money.class) {
            return null;
        }

        return (ParamConverter<T>) new ParamConverter<Money>() {

            @Override
            public Money fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }

                try {
                    return Money.parse(value);
                } catch (IllegalArgumentException ex) {
                    throw new jakarta.ws.rs.BadRequestException(
                            "Invalid money format. Use: amount currency", ex
                    );
                }
            }

            @Override
            public String toString(Money value) {
                return value.toString();
            }
        };
    }
}