package no.unit.nva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.unit.nva.cristin.common.Utils;
import nva.commons.core.JsonSerializable;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static no.unit.nva.cristin.model.JsonPropertyNames.ACRONYM;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.PART_OF;
import static no.unit.nva.cristin.model.JsonPropertyNames.SUBUNITS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;


@SuppressWarnings("PMD.BeanMembersShouldSerialize")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({ID, TYPE, NAME, ACRONYM, PART_OF, SUBUNITS})
public class Organization implements JsonSerializable {


    @JsonProperty(ID)
    private final URI id;
    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty(NAME)
    private final Map<String, String> name;
    @JsonProperty(ACRONYM)
    private final String acronym;
    @JsonProperty(PART_OF)
    private final Set<Organization> partOf;
    @JsonProperty(SUBUNITS)
    private final Set<Organization> subUnits;

    @JsonCreator
    public Organization(@JsonProperty(ID) URI id,
                        @JsonProperty(NAME) Map<String, String> name,
                        @JsonProperty(ACRONYM) String acronym,
                        @JsonProperty(PART_OF) Set<Organization> partOf,
                        @JsonProperty(SUBUNITS) Set<Organization> subUnits) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.partOf = partOf;
        this.subUnits = subUnits;
    }

    private Organization(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.acronym = builder.acronym;
        this.partOf = builder.partOf;
        this.subUnits = builder.subUnits;
    }


    public String getAcronym() {
        return acronym;
    }

    public URI getId() {
        return id;
    }

    public Map<String, String> getName() {
        return Utils.nonEmptyOrDefault(name);
    }

    public Set<Organization> getPartOf() {
        return partOf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Organization)) {
            return false;
        }
        Organization that = (Organization) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getAcronym(), that.getAcronym())
                && Objects.equals(getPartOf(), that.getPartOf());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getAcronym(), getPartOf());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    public static final class Builder {

        private URI id;
        private Map<String, String> name;
        private String acronym;
        private Set<Organization> partOf;
        private Set<Organization> subUnits;

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withName(Map<String, String> name) {
            this.name = name;
            return this;
        }

        public Builder withAcronym(String acronym) {
            this.acronym = acronym;
            return this;
        }

        public Builder withPartOf(Set<Organization> partOf) {
            this.partOf = partOf;
            return this;
        }

        public Builder withSubUnits(Set<Organization> subUnits) {
            this.subUnits = subUnits;
            return this;
        }

        public Organization build() {
            return new Organization(this);
        }
    }

}
