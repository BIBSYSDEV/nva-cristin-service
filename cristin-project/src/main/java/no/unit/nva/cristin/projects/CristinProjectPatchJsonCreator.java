package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;

public class CristinProjectPatchJsonCreator {

    private final transient ObjectNode input;
    private final transient ObjectNode output;


    public CristinProjectPatchJsonCreator(ObjectNode objectNode) {
        input = objectNode;
        output = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Class for creating json matching Cristin schema for Project update.
     */
    public CristinProjectPatchJsonCreator create() {
        addFieldIfPresent(ACADEMIC_SUMMARY);
        addFieldIfPresent(ALTERNATIVE_TITLES);
        addFieldIfPresent(CONTRIBUTORS);
        addFieldIfPresent(COORDINATING_INSTITUTION);
        addFieldIfPresent(END_DATE);
        addFieldIfPresent(LANGUAGE);
        addFieldIfPresent(POPULAR_SCIENTIFIC_SUMMARY);
        addFieldIfPresent(START_DATE);
        addFieldIfPresent(STATUS);
        addFieldIfPresent(TITLE);
        return this;
    }

    private void addFieldIfPresent(String fieldName) {
        if (input.has(fieldName)) {
            output.set(fieldName, input.get(fieldName));
        }
    }

    public ObjectNode getOutput() {
        return output;
    }

}
