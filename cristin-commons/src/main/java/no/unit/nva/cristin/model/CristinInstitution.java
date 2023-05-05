package no.unit.nva.cristin.model;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.nvaIdentifierToCristinIdentifier;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.model.Organization;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinInstitution {

    private String cristinInstitutionId;
    private Map<String, String> institutionName;
    private String url;

    public CristinInstitution() {

    }

    public CristinInstitution(String identifier) {
        this.cristinInstitutionId = identifier;
    }

    public String getCristinInstitutionId() {
        return cristinInstitutionId;
    }

    public void setCristinInstitutionId(String cristinInstitutionId) {
        this.cristinInstitutionId = cristinInstitutionId;
    }

    public Map<String, String> getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(Map<String, String> institutionName) {
        this.institutionName = institutionName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Creates an Organization model based on this CristinInstitution model.
     *
     * @return Organization model
     */
    public Organization toOrganization() {
        URI id = getNvaApiId(getCristinInstitutionId(), ORGANIZATION_PATH);
        return new Organization.Builder().withId(id).withName(getInstitutionName()).withLabels(getInstitutionName())
                   .build();
    }

    /**
     * Creates a CristinInstitution from an Organization model.
     *
     * @return CristinInstitution
     */
    public static CristinInstitution fromOrganization(Organization organization) {
        if (isNull(organization) || isNull(organization.getId())) {
            return null;
        }
        CristinInstitution institution = new CristinInstitution();
        institution.setInstitutionName(organization.getName());
        institution.setUrl(nvaIdentifierToCristinIdentifier(organization.getId(), INSTITUTION_PATH).toString());
        institution.setCristinInstitutionId(extractLastPathElement(organization.getId()));
        return institution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinInstitution)) {
            return false;
        }
        CristinInstitution that = (CristinInstitution) o;
        return Objects.equals(getCristinInstitutionId(), that.getCristinInstitutionId())
               && Objects.equals(getInstitutionName(), that.getInstitutionName())
               && Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCristinInstitutionId(), getInstitutionName(), getUrl());
    }
}
