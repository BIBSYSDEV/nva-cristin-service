package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.projects.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.IDENTITY;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nva.commons.core.JacocoGenerated;

//import java.time.Instant;

@JacocoGenerated
@JsonPropertyOrder({TYPE, IDENTITY, AFFILIATION})
public class NvaContributor {

    private String type;
    //private Instant startDate;
    //private Instant endDate;
    private NvaPerson identity;
    private NvaOrganization affiliation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NvaPerson getIdentity() {
        return identity;
    }

    public void setIdentity(NvaPerson identity) {
        this.identity = identity;
    }

    public NvaOrganization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(NvaOrganization affiliation) {
        this.affiliation = affiliation;
    }
}
