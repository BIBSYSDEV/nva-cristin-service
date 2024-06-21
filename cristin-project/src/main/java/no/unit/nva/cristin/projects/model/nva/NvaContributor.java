package no.unit.nva.cristin.projects.model.nva;

import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.model.Organization;

public class NvaContributor implements JsonSerializable {

    private String type;
    private Person identity;
    private Organization affiliation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Person getIdentity() {
        return identity;
    }

    public void setIdentity(Person identity) {
        this.identity = identity;
    }

    public Organization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Organization affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getIdentity(), getAffiliation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaContributor that)) {
            return false;
        }
        return Objects.equals(getType(), that.getType())
               && Objects.equals(getIdentity(), that.getIdentity())
               && Objects.equals(getAffiliation(), that.getAffiliation());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
