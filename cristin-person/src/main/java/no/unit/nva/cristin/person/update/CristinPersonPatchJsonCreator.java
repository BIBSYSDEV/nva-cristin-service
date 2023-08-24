package no.unit.nva.cristin.person.update;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.TypedValue;

public class CristinPersonPatchJsonCreator {

    public static final String CRISTIN_FIRST_NAME = "first_name";
    public static final String CRISTIN_SURNAME = "surname";
    public static final String CRISTIN_FIRST_NAME_PREFERRED = "first_name_preferred";
    public static final String CRISTIN_SURNAME_PREFERRED = "surname_preferred";

    private final transient ObjectNode input;
    private final transient ObjectNode output;

    /**
     * Class for creating json matching Cristin schema.
     */
    public CristinPersonPatchJsonCreator(ObjectNode input) {
        this.input = input;
        output = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Create Cristin json object.
     */
    public CristinPersonPatchJsonCreator create() {
        addOrcid();
        addFirstName();
        addLastName();
        addPreferredFirstName();
        addPreferredLastName();
        addReserved();
        addEmployments();
        addKeywords();
        addBackgroundIfPresent();

        return this;
    }

    /**
     * Create Cristin json object from values allowed to be changed by the user themselves.
     */
    public CristinPersonPatchJsonCreator createWithAllowedUserModifiableData() {
        addOrcid();

        return this;
    }

    public ObjectNode getOutput() {
        return output;
    }

    private void addOrcid() {
        if (input.has(ORCID)) {
            ObjectNode identifier = OBJECT_MAPPER.createObjectNode();
            identifier.set(ID, input.get(ORCID));
            output.set(ORCID, identifier);
        }
    }

    private void addFirstName() {
        if (input.has(FIRST_NAME)) {
            output.set(CRISTIN_FIRST_NAME, input.get(FIRST_NAME));
        }
    }

    private void addLastName() {
        if (input.has(LAST_NAME)) {
            output.set(CRISTIN_SURNAME, input.get(LAST_NAME));
        }
    }

    private void addPreferredFirstName() {
        if (input.has(PREFERRED_FIRST_NAME)) {
            output.set(CRISTIN_FIRST_NAME_PREFERRED, input.get(PREFERRED_FIRST_NAME));
        }
    }

    private void addPreferredLastName() {
        if (input.has(PREFERRED_LAST_NAME)) {
            output.set(CRISTIN_SURNAME_PREFERRED, input.get(PREFERRED_LAST_NAME));
        }
    }

    private void addReserved() {
        if (input.has(RESERVED) && input.get(RESERVED).asBoolean()) {
            output.set(RESERVED, input.get(RESERVED));
        }
    }

    
    private void addEmployments() {
        if (input.has(EMPLOYMENTS) && !input.get(EMPLOYMENTS).isNull()) {
            var employmentsInCristinFormat =
                attempt(() -> {
                    var parsedInput =
                        asList(OBJECT_MAPPER.readValue(input.get(EMPLOYMENTS).toString(), Employment[].class));
                    return OBJECT_MAPPER.readTree(employmentsToCristinFormat(parsedInput).toString());
                }).orElseThrow();
            output.set(CRISTIN_EMPLOYMENTS, employmentsInCristinFormat);
        }
    }

    private List<CristinPersonEmployment> employmentsToCristinFormat(List<Employment> employments) {
        return employments.stream().map(Employment::toCristinEmployment).collect(Collectors.toList());
    }

    
    private void addKeywords() {
        if (input.has(KEYWORDS) && !input.get(KEYWORDS).isNull()) {
            var keywordsInCristinFormat =
                attempt(() -> {
                    var parsedInput =
                        asList(OBJECT_MAPPER.readValue(input.get(KEYWORDS).toString(), TypedValue[].class));
                    return OBJECT_MAPPER.readTree(keywordsToCristinFormat(parsedInput).toString());
                }).orElseThrow();
            output.set(KEYWORDS, keywordsInCristinFormat);
        }
    }

    private List<CristinTypedLabel> keywordsToCristinFormat(List<TypedValue> parsedInput) {
        return parsedInput.stream().map(elm -> new CristinTypedLabel(elm.getType(), null))
                   .collect(Collectors.toList());
    }

    private void addBackgroundIfPresent() {
        if (input.has(BACKGROUND)) {
            output.set(BACKGROUND, input.get(BACKGROUND));
        }
    }
}
