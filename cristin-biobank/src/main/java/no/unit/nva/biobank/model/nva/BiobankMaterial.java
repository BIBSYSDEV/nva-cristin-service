package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Objects;

public class BiobankMaterial implements JsonSerializable {
    private final String  code;
    @JsonPropertyOrder(alphabetic = true)
    private final Map<String, String> name;

    @ConstructorProperties({"code","name"})
    public BiobankMaterial(String code, Map<String, String> name) {
        this.code = code;
        this.name = name;
    }

    public BiobankMaterial(CristinBiobankMaterial cristinBiobankMaterial) {
        this.code = cristinBiobankMaterial.getMaterialCode();
        this.name = cristinBiobankMaterial.getMaterialName();
    }


    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BiobankMaterial)) {
            return false;
        }
        BiobankMaterial that = (BiobankMaterial) o;
        return Objects.equals(getCode(), that.getCode())
            && Objects.equals(getName(), that.getName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getCode(), getName());
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
