package no.unit.nva.cristin.person.orcid.model;

import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_PERSON_ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.unit.nva.cristin.person.model.cristin.CristinOrcid;

public record CristinPersonOrcid(@JsonProperty(CRISTIN_PERSON_ID) String cristinPersonId,
                                 @JsonProperty(ORCID) CristinOrcid orcid) {

    public static final String PERSON_ORCID_ID_URI_TEMPLATE = getNvaApiUri(PERSON_PATH_NVA) + "/%s";
    public static final String UNKNOWN = "UNKNOWN";

    public PersonOrcid toPersonOrcid() {
        var id = URI.create(String.format(PERSON_ORCID_ID_URI_TEMPLATE, cristinPersonId));
        var orcid = URI.create(String.format(PERSON_ORCID_ID_URI_TEMPLATE, orcid().getId().orElse(UNKNOWN)));

        return new PersonOrcid(id, orcid);
    }

}
