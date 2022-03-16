package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import org.json.JSONObject;

public class CristinPersonPatchJsonCreator {

    public static final String CRISTIN_FIRST_NAME = "first_name";
    public static final String CRISTIN_SURNAME = "surname";
    public static final String CRISTIN_FIRST_NAME_PREFERRED = "first_name_preferred";
    public static final String CRISTIN_SURNAME_PREFERRED = "surname_preferred";

    private final transient JSONObject input;
    private final transient JSONObject result;

    /**
     * Class for creating json matching Cristin schema.
     */
    public CristinPersonPatchJsonCreator(JSONObject input) {
        this.input = input;
        result = new JSONObject();
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

        return this;
    }

    public JSONObject getResult() {
        return result;
    }

    private void addOrcid() {
        if (input.has(ORCID)) {
            JSONObject identifier = new JSONObject();
            identifier.put(ID, input.opt(ORCID));
            result.put(ORCID, identifier);
        }
    }

    private void addFirstName() {
        if (input.has(FIRST_NAME)) {
            result.put(CRISTIN_FIRST_NAME, input.get(FIRST_NAME));
        }
    }

    private void addLastName() {
        if (input.has(LAST_NAME)) {
            result.put(CRISTIN_SURNAME, input.get(LAST_NAME));
        }
    }

    private void addPreferredFirstName() {
        if (input.has(PREFERRED_FIRST_NAME)) {
            result.put(CRISTIN_FIRST_NAME_PREFERRED, input.get(PREFERRED_FIRST_NAME));
        }
    }

    private void addPreferredLastName() {
        if (input.has(PREFERRED_LAST_NAME)) {
            result.put(CRISTIN_SURNAME_PREFERRED, input.get(PREFERRED_LAST_NAME));
        }
    }

    private void addReserved() {
        if (input.has(RESERVED)) {
            result.put(RESERVED, input.get(RESERVED));
        }
    }
}
