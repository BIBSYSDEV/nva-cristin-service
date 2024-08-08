package no.unit.nva.biobank.model.cristin;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.biobank.model.cristin.adapter.CristinBiobankToBiobank;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinPerson;
import nva.commons.core.JacocoGenerated;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyFields"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinBiobank(@JsonProperty("cristin_biobank_id") String cristinBiobankId,
                             @JsonProperty("type") String type,
                             @JsonProperty("name") Map<String, String> name,
                             @JsonProperty("main_language") String mainLanguage,
                             @JsonProperty("start_date") Instant startDate,
                             @JsonProperty("store_until_date") Instant storeUntilDate,
                             @JsonProperty("status") String status,
                             @JsonProperty("created") CristinDateInfo created,
                             @JsonProperty("last_modified") CristinDateInfo lastModified,
                             @JsonProperty("coordinating_institution") CristinOrganization coordinatingInstitution,
                             @JsonProperty("status_name") Map<String, String> statusName,
                             @JsonProperty("type_name") Map<String, String> typeName,
                             @JsonProperty("coordinator") CristinPerson coordinator,
                             @JsonProperty("external_sources") Set<CristinExternalSource> externalSources,
                             @JsonProperty("approvals") List<CristinApproval> approvals,
                             @JsonProperty("biobank_materials") List<CristinBiobankMaterial> biobankMaterials,
                             @JsonProperty("biobank_id") String biobankId,
                             @JsonProperty("associated_project") CristinAssociatedProject associatedProject)
    implements JsonSerializable {

    @Override
    public Map<String, String> name() {
        return nonEmptyOrDefault(name);
    }

    @Override
    public Map<String, String> statusName() {
        return nonEmptyOrDefault(statusName);
    }

    @Override
    public Map<String, String> typeName() {
        return nonEmptyOrDefault(typeName);
    }

    @Override
    public Set<CristinExternalSource> externalSources() {
        return nonEmptyOrDefault(externalSources);
    }

    @Override
    public List<CristinApproval> approvals() {
        return nonEmptyOrDefault(approvals);
    }

    @Override
    public List<CristinBiobankMaterial> biobankMaterials() {
        return nonEmptyOrDefault(biobankMaterials);
    }

    public Biobank toBiobank() {
        return new CristinBiobankToBiobank().apply(this);
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }

}
