package no.unit.nva.cristin.funding.sources.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.ioutils.IoUtils;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingSource {
    private static final String CONTEXT_OBJECT = IoUtils.stringFromResources(Path.of("funding_context.json"));
    private static final String ID_FIELD = "id";
    private static final String CONTEXT_FIELD = "@context";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";

    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> labels;

    @JsonCreator
    public FundingSource(@JsonProperty(ID_FIELD) URI id,
                         @JsonProperty(IDENTIFIER_FIELD) String identifier,
                         @JsonProperty(NAME_FIELD) Map<String, String> labels) {
        this.id = id;
        this.identifier = identifier;
        this.labels = Collections.unmodifiableMap(labels);
    }

    public URI getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    @JsonGetter
    @JsonProperty(CONTEXT_FIELD)
    public Map<String, Object> getContext() {
        var type = new TypeReference<Map<String, Object>>(){};
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(CONTEXT_OBJECT, type)).orElseThrow();
    }
}
