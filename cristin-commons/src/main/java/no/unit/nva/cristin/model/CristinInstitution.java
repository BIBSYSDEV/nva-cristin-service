package no.unit.nva.cristin.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

import no.unit.nva.model.Organization;
import nva.commons.core.JacocoGenerated;

import static no.unit.nva.utils.UriUtils.buildUri;


@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinInstitution {

    private String cristinInstitutionId;
    private Map<String, String> institutionName;
    private String url;

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
     * Build a Organization datamodel from a CristinInstitution datamodel.
     *
     * @return a Organization converted from a CristinInstitution
     */
    public Organization toOrganization() {

        return new Organization.Builder()
                .withId(buildUri(Constants.CRISTIN_API_BASE_URL, Constants.INSTITUTION_PATH,
                        getCristinInstitutionId()))
                .withName(getInstitutionName()).build();
    }

}

