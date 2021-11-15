package no.unit.nva.cristin.model;

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

import static no.unit.nva.cristin.common.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({ID, TYPE, NAME, "acronym", "partOf"})
public class Organization implements JsonSerializable {

    private final URI id;
    @JsonPropertyOrder(alphabetic = true)
    private final Map<String, String> name;
    private final String acronym;
    private final Set<Organization> partOf;

    @JsonCreator
    public Organization(@JsonProperty("id") URI id,
                        @JsonProperty("name") Map<String, String> name,
                        @JsonProperty("acronym") String acronym,
                        @JsonProperty("partOf") Set<Organization> partOf) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.partOf = partOf;
    }

    private Organization(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.acronym = builder.acronym;
        this.partOf = builder.partOf;
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

        public Organization build() {
            return new Organization(this);
        }
    }

}
