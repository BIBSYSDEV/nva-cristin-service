package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class CreateCristinPersonHandler extends ApiGatewayHandler<Person, Person> {

    public static final String NATIONAL_IDENTITY_NUMBER = "NationalIdentityNumber";
    public static final String DUMMY_NATIONAL_IDENTITY_NUMBER = "12345612345";
    public static final String INVALID_PAYLOAD = "Invalid Payload";
    public static final String DUMMY_CRISTIN_ID = "12345";
    public static final String DUMMY_FIRST_NAME = "Kjell";
    public static final String DUMMY_SURNAME = "Olsen";

    @JacocoGenerated
    public CreateCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public CreateCristinPersonHandler(Environment environment) {
        super(Person.class, environment);
    }

    protected static Person dummyPerson() {
        CristinPerson cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(DUMMY_CRISTIN_ID);
        cristinPerson.setFirstName(DUMMY_FIRST_NAME);
        cristinPerson.setSurname(DUMMY_SURNAME);
        return cristinPerson.toPerson();
    }

    @Override
    protected Person processInput(Person input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        AccessUtils.validateAccess(requestInfo);

        Optional<TypedValue> nationalIdentityNumber = extractNationalIdentityNumber(input);

        if (nationalIdentityNumber.isPresent() && isValid(nationalIdentityNumber.get())) {
            return dummyPerson();
        }

        throw new BadRequestException(INVALID_PAYLOAD);
    }

    @Override
    protected Integer getSuccessStatusCode(Person input, Person output) {
        return HttpURLConnection.HTTP_OK;
    }

    private boolean isValid(TypedValue nationalIdentityNumber) {
        return DUMMY_NATIONAL_IDENTITY_NUMBER.equals(nationalIdentityNumber.getValue());
    }

    private Optional<TypedValue> extractNationalIdentityNumber(Person input) {
        Set<TypedValue> identifiers = Optional.ofNullable(input)
            .map(Person::getIdentifiers)
            .orElse(Collections.emptySet());

        return identifiers.stream()
            .filter(isNationalIdentityNumber())
            .findFirst();
    }

    private Predicate<TypedValue> isNationalIdentityNumber() {
        return typedValue -> NATIONAL_IDENTITY_NUMBER.equals(typedValue.getType());
    }
}
