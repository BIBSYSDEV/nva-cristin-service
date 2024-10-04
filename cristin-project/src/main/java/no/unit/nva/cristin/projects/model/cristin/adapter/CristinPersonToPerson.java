package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
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
            extractIdIfIdentified(cristinPerson.identifiedCristinPerson(), cristinPerson.cristinPersonId()),
            cristinPerson.firstName(),
            cristinPerson.surname(),
            cristinPerson.email(),
            cristinPerson.phone());
    }

    private URI extractIdIfIdentified(Boolean identified, String cristinPersonId) {
        if (identifiedFieldNotPresent(identified) || identified) {
            return getNvaApiId(cristinPersonId, PERSON_PATH_NVA);
        }
        return null;
    }

    private boolean identifiedFieldNotPresent(Boolean identified) {
        return isNull(identified);
    }

}
