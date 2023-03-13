package no.unit.nva.cristin.model;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

public interface IParameterKey {

    String getNvaKey();

    String getKey();

    String getPattern();

    String getErrorMessage();

    boolean isEncode();

    static Predicate<IParameterKey> hasValidValue(String value) {
        return f -> {
            var encoded = f.isEncode() ? URLDecoder.decode(value, StandardCharsets.UTF_8) : value;
            return encoded.matches(f.getPattern());
        };
    }

    static Predicate<IParameterKey> equalTo(String name) {
        return key -> name.equals(key.getKey()) || name.equals(key.getNvaKey());
    }
}
