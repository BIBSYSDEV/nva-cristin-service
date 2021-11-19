package no.unit.nva.cristin.model;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_BASE;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Map;
import no.unit.nva.model.Organization;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;


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
     * Creates an Organization model based on this CristinInstitution model.
     *
     * @return Organization model
     */
    public Organization toOrganization() {

        final URI id = new UriWrapper(HTTPS, CRISTIN_API_BASE).addChild(INSTITUTION_PATH)
            .addChild(getCristinInstitutionId()).getUri();
        return new Organization.Builder()
            .withId(id)
            .withName(getInstitutionName()).build();
    }
}

