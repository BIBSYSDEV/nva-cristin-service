package no.unit.nva.cristin.person.affiliations.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.model.Context;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@JacocoGenerated
public class PositionCodes implements JsonSerializable {

    @JsonProperty("@context")
    private final Context context;
    private final Set<PositionCode> codes;

    /**
     * Creates a Position Code wrapper for serialization to client.
     */
    @JsonCreator
    public PositionCodes(@JsonProperty("@context") Context context, @JsonProperty("codes") Set<PositionCode> codes) {
        this.context = context;
        this.codes = codes;
    }

    public Context getContext() {
        return context;
    }

    public Set<PositionCode> getCodes() {
        return Utils.nonEmptyOrDefault(codes);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PositionCodes)) {
            return false;
        }
        PositionCodes that = (PositionCodes) o;
        return Objects.equals(getContext(), that.getContext())
            && getCodes().equals(that.getCodes());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getCodes());
    }
}
