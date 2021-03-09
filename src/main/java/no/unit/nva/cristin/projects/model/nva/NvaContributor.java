package no.unit.nva.cristin.projects.model.nva;

//import java.time.Instant;

import nva.commons.core.JacocoGenerated;

@JacocoGenerated
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
