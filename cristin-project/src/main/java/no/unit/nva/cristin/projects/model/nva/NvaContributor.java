package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import no.unit.nva.commons.json.JsonSerializable;

public record NvaContributor(@JsonProperty(IDENTITY) Person identity,
                             @JsonProperty(ROLES) List<Role> roles) implements JsonSerializable {

    public static final String IDENTITY = "identity";
    public static final String ROLES = "roles";

    @Override
    public List<Role> roles() {
        return nonEmptyOrDefault(roles);
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
