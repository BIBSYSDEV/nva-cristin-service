package no.unit.nva.cristin.funding.sources.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.ioutils.IoUtils;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record FundingSource(@JsonProperty(ID_FIELD) URI id,
                            @JsonProperty(IDENTIFIER_FIELD) String identifier,
                            @JsonProperty(LABELS) Map<String, String> labels) {

    private static final String CONTEXT_OBJECT = IoUtils.stringFromResources(Path.of("funding_context.json"));
    private static final String ID_FIELD = "id";
    private static final String CONTEXT_FIELD = "@context";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";

    /**
     * Get labels.
     *
     * @deprecated Use {@link FundingSource#labels ()}} instead.
     */
    @Deprecated
    @JsonGetter
    @JsonProperty(NAME_FIELD)
    public Map<String, String> getName() {
        return nonEmptyOrDefault(labels);
    }

    @Override
    public Map<String, String> labels() {
        return nonEmptyOrDefault(labels);
    }

    @JsonGetter
    @JsonProperty(CONTEXT_FIELD)
    public Map<String, Object> getContext() {
        var type = new TypeReference<Map<String, Object>>() {};
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(CONTEXT_OBJECT, type)).orElseThrow();
    }

}
