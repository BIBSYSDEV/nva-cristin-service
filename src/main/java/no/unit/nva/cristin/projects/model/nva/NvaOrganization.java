package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_BASE_URL;
import static no.unit.nva.cristin.projects.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import no.unit.nva.cristin.projects.model.cristin.CristinInstitution;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonPropertyOrder({ID, TYPE, NAME})
public class NvaOrganization {

    @JsonIgnore
    private static final String ORGANIZATION_TYPE = "Organization";

    private URI id;
    private String type;
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> name;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    /**
     * Build a NvaOrganization datamodel from a CristinInstitution datamodel.
     *
     * @param cristinInstitution the model to convert from
     * @return a NvaOrganization converted from a CristinInstitution
     */
    public static NvaOrganization fromCristinInstitution(CristinInstitution cristinInstitution) {
        if (cristinInstitution == null) {
            return null;
        }

        NvaOrganization nvaOrganization = new NvaOrganization();
        nvaOrganization.setId(buildUri(CRISTIN_API_BASE_URL, INSTITUTION_PATH,
            cristinInstitution.getCristinInstitutionId()));
        nvaOrganization.setType(ORGANIZATION_TYPE);
        nvaOrganization.setName(cristinInstitution.getInstitutionName());
        return nvaOrganization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaOrganization)) {
            return false;
        }
        NvaOrganization that = (NvaOrganization) o;
        return getId().equals(that.getId())
                && getType().equals(that.getType())
                && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getName());
    }
}
