package no.unit.nva.cristin.model;

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

@SuppressWarnings({"unused", "PMD.BeanMembersShouldSerialize"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({ID, TYPE, NAME, "acronym", "partOf"})
public class Organization implements JsonSerializable {

    private URI id;
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> name;
    private String acronym;
    private Set<Organization> partOf;

    private Organization(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        setAcronym(builder.acronym);
        setPartOf(builder.partOf);
    }

    public Organization() {
    }


    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public Map<String, String> getName() {
        return Utils.nonEmptyOrDefault(name);
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Set<Organization> getPartOf() {
        return partOf;
    }

    public void setPartOf(Set<Organization> partOf) {
        this.partOf = partOf;
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
