package no.unit.nva.cristin.projects.model.cristin.adapter;

import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.Person;

public class CristinPersonToPerson implements Function<CristinPerson, Person> {

    @Override
    public Person apply(CristinPerson cristinPerson) {
        return Optional.ofNullable(cristinPerson)
                   .map(this::convert)
                   .orElse(null);
    }

    private Person convert(CristinPerson cristinPerson) {
        return new Person(
            convertId(cristinPerson.getCristinPersonId()),
            cristinPerson.getFirstName(),
            cristinPerson.getSurname(),
            cristinPerson.getEmail(),
            cristinPerson.getPhone());
    }

    private URI convertId(String cristinPersonId) {
        return getNvaApiId(cristinPersonId, PERSON_PATH_NVA);
    }

}
