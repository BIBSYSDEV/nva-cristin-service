package no.unit.nva.cristin.person.affiliations.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.common.Utils;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class PositionCodes implements JsonSerializable {

    @JsonProperty(CONTEXT)
    @JsonInclude(NON_NULL)
    private final URI context;
    private final Set<PositionCode> positions;

    /**
     * Creates a Position Code wrapper for serialization to client.
     */
    @JsonCreator
    public PositionCodes(@JsonProperty("@context") URI context, @JsonProperty("codes") Set<PositionCode> codes) {
        this.context = context;
        this.positions = codes;
    }

    public URI getContext() {
        return context;
    }

    public Set<PositionCode> getPositions() {
        return Utils.nonEmptyOrDefault(positions);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PositionCodes that)) {
            return false;
        }
        return Objects.equals(getContext(), that.getContext())
            && getPositions().equals(that.getPositions());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getPositions());
    }
}
