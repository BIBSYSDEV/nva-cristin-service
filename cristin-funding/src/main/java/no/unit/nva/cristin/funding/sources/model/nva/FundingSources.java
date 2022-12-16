package no.unit.nva.cristin.funding.sources.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class FundingSources {
    private static final String SOURCES_FIELD_NAME = "sources";

    @JsonProperty(CONTEXT)
    private final URI context;
    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(SOURCES_FIELD_NAME)
    private final List<FundingSource> sources;

    @JsonCreator
    public FundingSources(@JsonProperty(CONTEXT) URI context,
                          @JsonProperty(ID) URI id,
                          @JsonProperty(SOURCES_FIELD_NAME) List<FundingSource> sources) {
        this.context = context;
        this.id = id;
        this.sources = Collections.unmodifiableList(sources);
    }

    public URI getContext() {
        return context;
    }

    public URI getId() {
        return id;
    }

    public List<FundingSource> getSources() {
        return sources;
    }
}
