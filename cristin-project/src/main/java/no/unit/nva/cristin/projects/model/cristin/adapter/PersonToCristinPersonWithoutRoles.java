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
        var cristinPerson = new CristinPerson();

        cristinPerson.setCristinPersonId(toCristinPersonIdentity(person.getId()));
        cristinPerson.setUrl(nvaIdentifierToCristinIdentifier(person.getId(), PERSON_PATH).toString());
        cristinPerson.setFirstName(person.getFirstName());
        cristinPerson.setSurname(person.getLastName());
        cristinPerson.setEmail(person.getEmail());
        cristinPerson.setPhone(person.getPhone());

        return cristinPerson;
    }

    private String toCristinPersonIdentity(URI id) {
        return extractLastPathElement(id);
    }

}
