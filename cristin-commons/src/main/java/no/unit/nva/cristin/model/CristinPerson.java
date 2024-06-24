package no.unit.nva.cristin.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinPerson(String cristinPersonId,
                            String firstName,
                            String surname,
                            String url,
                            String email,
                            String phone,
                            List<CristinRole> roles) {

    // Builder
    public static class Builder {
        private String cristinPersonId;
        private String firstName;
        private String surname;
        private String url;
        private String email;
        private String phone;
        private List<CristinRole> roles;

        public Builder withCristinPersonId(String cristinPersonId) {
            this.cristinPersonId = cristinPersonId;
            return this;
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

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder withRoles(List<CristinRole> roles) {
            this.roles = roles;
            return this;
        }

        public CristinPerson build() {
            return new CristinPerson(cristinPersonId, firstName, surname, url, email, phone, roles);
        }
    }

    // Copy using Builder
    public Builder copy() {
        return new CristinPerson.Builder()
            .withCristinPersonId(cristinPersonId)
            .withFirstName(firstName)
            .withSurname(surname)
            .withUrl(url)
            .withEmail(email)
            .withPhone(phone)
            .withRoles(roles);
    }

}

