package no.unit.nva.cristin.organization;


import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Objects;

public class InstitutionResponse {

    private URI id;
    private String name;
    private String acronym;

    private InstitutionResponse(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        setAcronym(builder.acronym);
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstitutionResponse that = (InstitutionResponse) o;
        return Objects.equals(id, that.getId())
            && Objects.equals(name, that.getName())
            && Objects.equals(acronym, that.getAcronym());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(id, name, acronym);
    }

    public static final class Builder {

        private URI id;
        private String name;
        private String acronym;

        public Builder() {
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withAcronym(String acronym) {
            this.acronym = acronym;
            return this;
        }

        public InstitutionResponse build() {
            return new InstitutionResponse(this);
        }
    }
}
