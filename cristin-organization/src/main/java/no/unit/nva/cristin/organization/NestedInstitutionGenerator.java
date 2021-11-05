package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.databind.JsonNode;
import no.unit.nva.cristin.organization.dto.InstitutionBaseDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;

import java.net.URI;


public class NestedInstitutionGenerator {


    public NestedInstitutionGenerator() {

    }

    /**
     * Returns a JSON-LD string representing a nested institution.
     *
     * @return A JSON-LD string
     */
    public JsonNode getNestedInstitution() {
        return null;
    }

    /**
     * Adds a unit to the model, creating a triple for the name and a triple for the parent relation.
     *
     * @param uri           The URI of the unit
     * @param subSubUnitDto The object representing the unit
     */
    public void addUnitToModel(URI uri, SubSubUnitDto subSubUnitDto) {

    }

    /**
     * Adds a single triple for the top-level institution, using the corresponding unit URI as subject.
     *
     * @param institution the institution unit.
     */
    public void setInstitution(InstitutionBaseDto institution) {
    }
}
