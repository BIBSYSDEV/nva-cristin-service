package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.utils.CustomInstantSerializer;
import nva.commons.core.JacocoGenerated;

import java.time.Instant;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPersonEmployment {

    private String id;
    private CristinOrganization affiliation;
    private Boolean active;
    private CristinPositionCode position;
    @JsonSerialize(using= CustomInstantSerializer.class)
    private Instant startDate;
    @JsonSerialize(using= CustomInstantSerializer.class)
    private Instant endDate;
    private Double ftePercentage;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CristinOrganization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(CristinOrganization affiliation) {
        this.affiliation = affiliation;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public CristinPositionCode getPosition() {
        return position;
    }

    public void setPosition(CristinPositionCode position) {
        this.position = position;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Double getFtePercentage() {
        return ftePercentage;
    }

    public void setFtePercentage(Double ftePercentage) {
        this.ftePercentage = ftePercentage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
