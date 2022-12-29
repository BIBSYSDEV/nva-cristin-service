package no.unit.nva.cristin.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.unit.nva.utils.DateInfo;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Biobank {
    private static final String ID_FIELD = "id";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";

    private static final String MODIFIED_SINCE = "modified_since";
    private static final String PROJECT_IDENTIFIER = "project";
    private static final String TYPE = "type";
    private static final String LANGUAGE_KEY = "lang";
    private static final String PAGE_KEY = "page";
    private static final String PER_PAGE_KEY = "per_page";



    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;

    @JsonProperty(MODIFIED_SINCE)
    private final DateInfo modifiedSince;

    @JsonCreator
    public Biobank(@JsonProperty(ID_FIELD) URI id,
                   @JsonProperty(IDENTIFIER_FIELD) String identifier,
                   @JsonProperty(NAME_FIELD) Map<String, String> name, DateInfo modifiedSince) {
        this.id = id;
        this.identifier = identifier;
        this.name = Collections.unmodifiableMap(name);
        this.modifiedSince = modifiedSince;
    }

    public URI getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getName() {
        return name;
    }
}
