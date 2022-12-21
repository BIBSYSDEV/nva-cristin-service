package no.unit.nva.cristin.biobank.model.nva;

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
public final class Biobanks {
    //Not sure we need biobankS. But I assume we do, especially if we are going to reuse functionality in project.
    private static final String SOURCES_FIELD_NAME = "sources";

    @JsonProperty(CONTEXT)
    private final URI context;
    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(SOURCES_FIELD_NAME)
    private final List<Biobank> sources;

    @JsonCreator
    public Biobanks(@JsonProperty(CONTEXT) URI context,
                          @JsonProperty(ID) URI id,
                          @JsonProperty(SOURCES_FIELD_NAME) List<Biobank> sources) {
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

    public List<Biobank> getSources() {
        return sources;
    }
}