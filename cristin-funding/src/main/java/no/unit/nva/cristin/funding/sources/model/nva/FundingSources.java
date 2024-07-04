package no.unit.nva.cristin.funding.sources.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.List;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record FundingSources(@JsonProperty(CONTEXT) URI context,
                             @JsonProperty(ID) URI id,
                             @JsonProperty(SOURCES_FIELD_NAME) List<FundingSource> sources) {

    private static final String SOURCES_FIELD_NAME = "sources";

    @Override
    public List<FundingSource> sources() {
        return nonEmptyOrDefault(sources);
    }

}
