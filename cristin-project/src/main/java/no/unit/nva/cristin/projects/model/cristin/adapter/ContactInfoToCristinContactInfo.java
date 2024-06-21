package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.projects.model.cristin.CristinContactInfo;
import no.unit.nva.cristin.projects.model.nva.ContactInfo;

public class ContactInfoToCristinContactInfo implements Function<ContactInfo, CristinContactInfo> {

    @Override
    public CristinContactInfo apply(ContactInfo contactInfo) {
        return Optional.ofNullable(contactInfo)
                   .map(this::convert)
                   .orElse(null);
    }

    private CristinContactInfo convert(ContactInfo contactInfo) {
        return new CristinContactInfo(contactInfo.getContactPerson(),
                                      contactInfo.getOrganization(),
                                      contactInfo.getEmail(),
                                      contactInfo.getPhone());
    }
}
