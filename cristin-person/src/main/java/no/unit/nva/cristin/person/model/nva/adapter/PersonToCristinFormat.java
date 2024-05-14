package no.unit.nva.cristin.person.model.nva.adapter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.distinctByKey;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.cristin.CristinNviInstitutionUnit;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.cristin.CristinPersonNvi;
import no.unit.nva.cristin.person.model.cristin.CristinPersonSummary;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.utils.UriUtils;

public class PersonToCristinFormat implements Function<Person, CristinPerson> {

    @Override
    public CristinPerson apply(Person person) {
        if (isNull(person)) {
            return null;
        }

        var cristinPerson = new CristinPerson();

        var namesMap = convertTypedValuesToMap(person.names());
        cristinPerson.setFirstName(namesMap.get(CristinPerson.FIRST_NAME));
        cristinPerson.setSurname(namesMap.get(CristinPerson.LAST_NAME));
        cristinPerson.setFirstNamePreferred(namesMap.get(CristinPerson.PREFERRED_FIRST_NAME));
        cristinPerson.setSurnamePreferred(namesMap.get(CristinPerson.PREFERRED_LAST_NAME));

        var identifierMap = convertTypedValuesToMap(person.identifiers());
        cristinPerson.setNorwegianNationalId(identifierMap.get(NATIONAL_IDENTITY_NUMBER));

        cristinPerson.setDetailedAffiliations(mapEmploymentsToCristinEmployments(person.employments()));
        cristinPerson.setReserved(person.reserved());
        cristinPerson.setKeywords(extractCristinTypedLabel(person.keywords()));
        cristinPerson.setBackground(person.background());

        if (nonNull(person.nvi())) {
            var cristinPersonNvi = new CristinPersonNvi(extractVerifiedById(person.nvi()),
                                                        extractVerifiedAtId(person.nvi()),
                                                        null);
            cristinPerson.setPersonNvi(cristinPersonNvi);
        }

        if (nonNull(person.contactDetails())) {
            cristinPerson.setTel(person.contactDetails().getTelephone().orElse(null));
            cristinPerson.setEmail(person.contactDetails().getEmail().orElse(null));
            cristinPerson.setWebPage(person.contactDetails().getWebPage().orElse(null));
        }

        cristinPerson.setPlace(person.place());
        cristinPerson.setCollaboration(person.collaboration());
        cristinPerson.setCountries(extractCristinTypedLabel(person.countries()));

        return cristinPerson;
    }

    private CristinNviInstitutionUnit extractVerifiedAtId(PersonNvi nvi) {
        var organizationHavingVerifiedId = UriUtils.extractLastPathElement(nvi.verifiedAt().getId());
        return new CristinNviInstitutionUnit(null,
                                             CristinUnit.fromCristinUnitIdentifier(organizationHavingVerifiedId));
    }

    private CristinPersonSummary extractVerifiedById(PersonNvi nvi) {
        var personHavingVerifiedId = UriUtils.extractLastPathElement(nvi.verifiedBy().id());
        return CristinPersonSummary.builder().withCristinPersonId(personHavingVerifiedId).build();
    }

    private List<CristinTypedLabel> extractCristinTypedLabel(Set<TypedLabel> typedLabels) {
        return typedLabels.stream()
                   .filter(label -> nonNull(label.getType()))
                   .map(label -> new CristinTypedLabel(label.getType().toUpperCase(Locale.ROOT), null))
                   .collect(Collectors.toList());
    }

    private Map<String, String> convertTypedValuesToMap(Set<TypedValue> typedValueSet) {
        return typedValueSet.stream()
                   .filter(TypedValue::hasData)
                   .filter(distinctByKey(TypedValue::getType))
                   .collect(Collectors.toMap(TypedValue::getType, TypedValue::getValue));
    }

    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    private List<CristinPersonEmployment> mapEmploymentsToCristinEmployments(Set<Employment> employments) {
        if (isNull(employments)) {
            return null;
        }

        return employments
                   .stream()
                   .map(Employment::toCristinEmployment)
                   .toList();
    }

}
