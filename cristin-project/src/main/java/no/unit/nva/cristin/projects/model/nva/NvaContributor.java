package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.unit.nva.model.Organization;

import java.util.Objects;

import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTITY;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;

@SuppressWarnings("unused")
@JsonPropertyOrder({TYPE, IDENTITY, AFFILIATION})
public class NvaContributor {

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaContributor)) {
            return false;
        }
        NvaContributor that = (NvaContributor) o;
        return getType().equals(that.getType())
                && getIdentity().equals(that.getIdentity())
                && getAffiliation().equals(that.getAffiliation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getIdentity(), getAffiliation());
    }
}
