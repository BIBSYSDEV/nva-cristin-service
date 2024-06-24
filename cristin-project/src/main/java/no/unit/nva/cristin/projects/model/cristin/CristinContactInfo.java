package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import no.unit.nva.cristin.projects.model.nva.ContactInfo;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinContactInfo(@JsonProperty(CRISTIN_CONTACT_PERSON) String contactPerson,
                                 @JsonProperty(INSTITUTION) String institution,
                                 @JsonProperty(EMAIL) String email,
                                 @JsonProperty(PHONE) String phone) {

    public static final String CRISTIN_CONTACT_PERSON = "contact_person";

    public static CristinContactInfo fromContactInfo(ContactInfo contactInfo) {
        return Optional.ofNullable(contactInfo)
                   .map(CristinContactInfo::convert)
                   .orElse(null);
    }

    private static CristinContactInfo convert(ContactInfo contactInfo) {
        return new CristinContactInfo(contactInfo.getContactPerson(),
                                      contactInfo.getOrganization(),
                                      contactInfo.getEmail(),
                                      contactInfo.getPhone());
    }

    public ContactInfo toContactInfo() {
        return new ContactInfo(contactPerson(),
                               institution(),
                               email(),
                               phone());
    }

}
