package no.unit.nva.cristin.keyword.query;

import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class QueryParamConverter {

    private final transient Map<String, String> input;
    private final transient Map<String, String> result;

    /**
     * Transforms request query params to cristin format for query params.
     */
    public QueryParamConverter(Map<String, String> input) {
        this.input = input;
        result = new ConcurrentHashMap<>();
    }

    /**
     * Does the actual conversion and puts the result into an object.
     */
    public QueryParamConverter convert() {
        Optional.ofNullable(input.get(NAME)).ifPresent(entry -> result.put(NAME, entry));
        Optional.ofNullable(input.get(PAGE)).ifPresent(entry -> result.put(PAGE, entry));
        Optional.ofNullable(input.get(NUMBER_OF_RESULTS)).ifPresent(entry -> result.put(CRISTIN_PER_PAGE_PARAM, entry));

        return this;
    }

    public Map<String, String> getResult() {
        return result;
    }
}
