package no.unit.nva.cristin.projects.model.cristin.adapter;

import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.nvaIdentifierToCristinIdentifier;
import java.net.URI;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.Person;

public class PersonToCristinPersonWithoutRoles implements Function<Person, CristinPerson> {

    @Override
    public CristinPerson apply(Person person) {
        return new CristinPerson.Builder()
                   .withCristinPersonId(toCristinPersonIdentity(person.getId()))
                   .withFirstName(person.getFirstName())
                   .withSurname(person.getLastName())
                   .withUrl(nvaIdentifierToCristinIdentifier(person.getId(), PERSON_PATH).toString())
                   .withEmail(person.getEmail())
                   .withPhone(person.getPhone())
                   .build();
    }

    private String toCristinPersonIdentity(URI id) {
        return extractLastPathElement(id);
    }

}
