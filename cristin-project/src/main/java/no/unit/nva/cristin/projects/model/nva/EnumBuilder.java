package no.unit.nva.cristin.projects.model.nva;

import java.util.Map;
import java.util.stream.Collectors;

public interface EnumBuilder<T, R extends Enum<R>> {

    R build(T classOfT);

    static Map<String, String> mapValuesReversed(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

}
