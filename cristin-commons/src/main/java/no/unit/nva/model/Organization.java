package no.unit.nva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nva.commons.core.JsonSerializable;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static no.unit.nva.cristin.model.JsonPropertyNames.ACRONYM;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.HAS_PART;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.PART_OF;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;


@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "PMD.LinguisticNaming"})
@JsonPropertyOrder({CONTEXT, ID, TYPE, NAME, ACRONYM, PART_OF, HAS_PART})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Organization implements JsonSerializable {

    public static final String ORGANIZATION_IDENTIFIER_PATTERN = "^(?:[0-9]+\\.){3}[0-9]{1,3}$";
    public static final String ORGANIZATION_CONTEXT = "https://api.dev.nva.aws.unit.no/cristin/organization";

    @JsonProperty(ID)
    private final URI id;
    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty(NAME)
    private final Map<String, String> name;
    @JsonProperty(ACRONYM)
    private final String acronym;
    @JsonProperty(PART_OF)
    private final Set<Organization> partOf;
    @JsonProperty(HAS_PART)
    private final Set<Organization> hasPart;
    @JsonProperty(CONTEXT)
    private String context;

    @SuppressWarnings("missingjavadocmethod")
    @JsonCreator
    public Organization(@JsonProperty(ID) URI id,
                        @JsonProperty(NAME) Map<String, String> name,
                        @JsonProperty(ACRONYM) String acronym,
                        @JsonProperty(PART_OF) Set<Organization> partOf,
                        @JsonProperty(HAS_PART) Set<Organization> hasPart) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.partOf = partOf;
        this.hasPart = hasPart;
    }

    private Organization(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.acronym = builder.acronym;
        this.partOf = builder.partOf;
        this.hasPart = builder.hasPart;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public URI getId() {
        return id;
    }

    public String getContext() {
        return context;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }

    public Set<Organization> getPartOf() {
        return partOf;
    }

    public Set<Organization> getHasPart() {
        return hasPart;
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
                && Objects.equals(getPartOf(), that.getPartOf())
                && Objects.equals(getHasPart(), that.getHasPart());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getAcronym(), getPartOf(), getHasPart());
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
        private Set<Organization> hasPart;

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

        public Builder withHasPart(Set<Organization> hasPart) {
            this.hasPart = hasPart;
            return this;
        }

        public Organization build() {
            return new Organization(this);
        }
    }

}


