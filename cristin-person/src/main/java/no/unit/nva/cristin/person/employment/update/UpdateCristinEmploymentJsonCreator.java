package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_INSTITUTION_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_UNIT_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.POSITION_CODE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment.POSITION;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.nva.Employment;
import nva.commons.core.paths.UriWrapper;

public class UpdateCristinEmploymentJsonCreator {

    private final transient ObjectNode input;
    private final transient ObjectNode output;

    /**
     * Class for creating json matching Cristin schema.
     */
    public UpdateCristinEmploymentJsonCreator(ObjectNode input) {
        this.input = input;
        output = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Create Cristin json object.
     */
    public UpdateCristinEmploymentJsonCreator create() {
        addAffiliation();
        addPositionCode();

        return this;
    }

    public ObjectNode getOutput() {
        return output;
    }

    private void addAffiliation() {
        if (input.has(ORGANIZATION)) {
            String orgId = getOrgId();
            ObjectNode institutionOrUnit = OBJECT_MAPPER.createObjectNode();

            if (CristinUnit.isCristinUnitIdentifier(orgId)) {
                institutionOrUnit.set(UNIT, addUnitAffiliation(orgId));
            } else if (Utils.isPositiveInteger(orgId)) {
                institutionOrUnit.set(INSTITUTION, addInstitutionAffiliation(orgId));
            }

            output.set(AFFILIATION, institutionOrUnit);
        }
    }

    private String getOrgId() {
        URI organization = parseUriField(ORGANIZATION);
        return extractLastPathElement(organization);
    }

    private ObjectNode addUnitAffiliation(String orgId) {
        return OBJECT_MAPPER.createObjectNode().put(CRISTIN_UNIT_ID, orgId);
    }

    private ObjectNode addInstitutionAffiliation(String orgId) {
        return OBJECT_MAPPER.createObjectNode().put(CRISTIN_INSTITUTION_ID, orgId);
    }

    private void addPositionCode() {
        if (input.has(TYPE)) {
            URI type = parseUriField(TYPE);
            String code = Employment.extractPositionCodeFromTypeUri(type).orElseThrow();
            ObjectNode positionNode = OBJECT_MAPPER.createObjectNode().put(POSITION_CODE, code);
            output.set(POSITION, positionNode);
        }
    }

    private URI parseUriField(String fieldName) {
        return UriWrapper.fromUri(input.get(fieldName).asText()).getUri();
    }
}
