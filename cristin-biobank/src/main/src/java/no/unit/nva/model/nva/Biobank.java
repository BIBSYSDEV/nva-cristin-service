package no.unit.nva.cristin.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Biobank {
    private static final String ID_FIELD = "id";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";

    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;

    @JsonCreator
    public Biobank(@JsonProperty(ID_FIELD) URI id,
                         @JsonProperty(IDENTIFIER_FIELD) String identifier,
                         @JsonProperty(NAME_FIELD) Map<String, String> name) {
        this.id = id;
        this.identifier = identifier;
        this.name = Collections.unmodifiableMap(name);
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
