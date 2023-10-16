package no.unit.nva.cristin.person.model.cristin;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.unit.nva.cristin.person.model.nva.PersonSummary;
import nva.commons.core.paths.UriWrapper;

public record CristinPersonSummary(@JsonProperty(FIRST_NAME) String firstName,
                                   @JsonProperty(SURNAME) String surname,
                                   @JsonProperty(URL_FIELD_NAME) String url,
                                   @JsonProperty(CRISTIN_PERSON_ID) String cristinPersonId) {

    public static final String FIRST_NAME = "first_name";
    public static final String SURNAME = "surname";
    public static final String URL_FIELD_NAME = "url";
    public static final String CRISTIN_PERSON_ID = "cristin_person_id";

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String firstName;
        private String surname;
        private String url;
        private String cristinPersonId;

        private Builder() {
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withCristinPersonId(String cristinPersonId) {
            this.cristinPersonId = cristinPersonId;
            return this;
        }

        public CristinPersonSummary build() {
            return new CristinPersonSummary(firstName, surname, url, cristinPersonId);
        }
    }

    @JsonIgnore
    public PersonSummary toPersonSummary() {
        return nonNull(cristinPersonId) ? new PersonSummary(extractIdUri(), firstName, surname) : null;
    }

    private URI extractIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA)
                   .addChild(cristinPersonId).getUri();
    }

}
