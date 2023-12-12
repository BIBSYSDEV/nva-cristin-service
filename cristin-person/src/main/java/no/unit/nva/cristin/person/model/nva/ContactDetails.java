package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class ContactDetails {

    private final String telephone;
    private final String email;
    private final String webPage;

    @JsonCreator
    public ContactDetails(@JsonProperty("telephone") String telephone, @JsonProperty("email") String email,
                          @JsonProperty("webPage") String webPage) {
        this.telephone = telephone;
        this.email = email;
        this.webPage = webPage;
    }

    public Optional<String> getTelephone() {
        return Optional.ofNullable(telephone);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getWebPage() {
        return Optional.ofNullable(webPage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContactDetails that)) {
            return false;
        }
        return Objects.equals(getTelephone(), that.getTelephone())
               && Objects.equals(getEmail(), that.getEmail())
               && Objects.equals(getWebPage(), that.getWebPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTelephone(), getEmail(), getWebPage());
    }

}
