package no.unit.nva.model.cristin;

import java.net.URI;

public class CristinCoordinator {

    public static final String CRISTIN_COORDINATOR_IDENTIFIER = "cristin_person_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String CRISTIN_COORDINATOR_ID = "url";

    private final String cristinPersonIdentifier;
    private final String firstName;
    private final String lastName;

    public String getCristinPersonIdentifier() {
        return cristinPersonIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public URI getId() {
        return id;
    }

    private final URI id;

    public CristinCoordinator(String cristinPersonIdentifier,
                              String firstName,
                              String lastName,
                              URI id) {
        this.cristinPersonIdentifier = cristinPersonIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }
}
