package no.unit.nva.cristin.person.create;

import java.util.Optional;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.PersonSummary;
import no.unit.nva.model.Organization;
import no.unit.nva.validation.Validator;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

public class PersonNviValidator implements Validator<PersonNvi> {

    public static final String INVALID_PERSON_ID =
        "Person NVI verification data must have a person identifier associated with it";
    public static final String INVALID_ORG_ID =
        "Person NVI verification data must have a organization identifier associated with it";

    @Override
    public void validate(PersonNvi personNvi) throws ApiGatewayException {
        Optional.ofNullable(personNvi)
            .map(PersonNvi::verifiedBy)
            .map(PersonSummary::id)
            .orElseThrow(() -> new BadRequestException(INVALID_PERSON_ID));
        Optional.of(personNvi)
            .map(PersonNvi::verifiedAt)
            .map(Organization::getId)
            .orElseThrow(() -> new BadRequestException(INVALID_ORG_ID));
    }

}
