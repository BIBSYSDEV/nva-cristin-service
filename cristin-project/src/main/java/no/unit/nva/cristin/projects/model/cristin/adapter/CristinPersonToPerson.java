package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.Person;

public class CristinPersonToPerson implements Function<CristinPerson, Person> {

    @Override
    public Person apply(CristinPerson cristinPerson) {
        if (isNull(cristinPerson)) {
            return null;
        }

        var id = getNvaApiId(cristinPerson.getCristinPersonId(), PERSON_PATH_NVA);

        return new Person(
            id,
            cristinPerson.getFirstName(),
            cristinPerson.getSurname(),
            cristinPerson.getEmail(),
            cristinPerson.getPhone());
    }

}
