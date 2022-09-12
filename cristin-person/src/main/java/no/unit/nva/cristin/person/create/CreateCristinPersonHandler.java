package no.unit.nva.cristin.person.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.employment.create.CreatePersonEmploymentValidator;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Employment;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static nva.commons.core.attempt.Try.attempt;

public class CreateCristinPersonHandler extends ApiGatewayHandler<Person, Person> {

    public static final String ERROR_MESSAGE_IDENTIFIER_NOT_VALID =
        String.format("%s is not valid", NATIONAL_IDENTITY_NUMBER);
    public static final String ERROR_MESSAGE_PAYLOAD_EMPTY = "Payload cannot be empty";
    public static final String ERROR_MESSAGE_MISSING_IDENTIFIER =
        String.format("Missing required identifier: %s", NATIONAL_IDENTITY_NUMBER);
    private static final Set<String> REQUIRED_NAMES = Set.of(CristinPerson.FIRST_NAME, CristinPerson.LAST_NAME);
    public static final String ERROR_MESSAGE_MISSING_REQUIRED_NAMES =
        String.format("Missing required names: %s", REQUIRED_NAMES);
    public static final String ERROR_MESSAGE_IDENTIFIERS_REPEATED =
            String.format("Unique identifier %s is repeated", NATIONAL_IDENTITY_NUMBER);
    public static final int MAX_NATIONAL_IDENTITY_NUMBER_COUNT = 1;

    private final transient CreateCristinPersonApiClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public CreateCristinPersonHandler() {
        this(new CreateCristinPersonApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    @JacocoGenerated
    public CreateCristinPersonHandler(CreateCristinPersonApiClient apiClient, Environment environment) {
        super(Person.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Person processInput(Person input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateContainsPayload(input);
        validateContainsRequiredNames(extractIdentifiers(input.getNames()));
        validateNoDuplicateNationalIdentifiers(input.getIdentifiers());
        validateContainsRequiredIdentifiers(extractIdentifiers(input.getIdentifiers()));
        validateValidIdentificationNumber(extractIdentificationNumber(input.getIdentifiers()));

        if (nonNull(input.getEmployments())) {
            AccessUtils.validateIdentificationNumberAccess(requestInfo);
            for (Employment employment : input.getEmployments()) {
                CreatePersonEmploymentValidator.validate(employment);
            }
        }

        if (suppliedInputPersonNinDoesNotMatchClientOwn(input, requestInfo)) {
            AccessUtils.validateIdentificationNumberAccess(requestInfo);
        }

        return apiClient.createPersonInCristin(input);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected Integer getSuccessStatusCode(Person input, Person output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    private void validateContainsPayload(Person input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_PAYLOAD_EMPTY);
        }
    }

    private Set<String> extractIdentifiers(Set<TypedValue> typedValues) {
        return Optional.ofNullable(typedValues).orElse(Collections.emptySet()).stream().map(TypedValue::getType)
            .collect(Collectors.toSet());
    }

    private void validateContainsRequiredNames(Set<String> names) throws BadRequestException {
        if (names.containsAll(REQUIRED_NAMES)) {
            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_MISSING_REQUIRED_NAMES);
    }

    private void validateContainsRequiredIdentifiers(Set<String> identificationNumbers) throws BadRequestException {
        if (identificationNumbers.contains(NATIONAL_IDENTITY_NUMBER)) {
            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_MISSING_IDENTIFIER);
    }

    private String extractIdentificationNumber(Set<TypedValue> identifiers) throws BadRequestException {
        return identifiers.stream().filter(TypedValue::hasData)
            .filter(elm -> NATIONAL_IDENTITY_NUMBER.equals(elm.getType()))
            .map(TypedValue::getValue).findFirst()
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_IDENTIFIER_NOT_VALID));
    }

    private void validateValidIdentificationNumber(String number) throws BadRequestException {
        if (FodselsnummerValidator.isValid(number)) {
            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_IDENTIFIER_NOT_VALID);
    }

    private void validateNoDuplicateNationalIdentifiers(Set<TypedValue> identifiers) throws BadRequestException {
        if (identifiers.stream().map(TypedValue::getType)
                .filter(NATIONAL_IDENTITY_NUMBER::equals)
                .collect(Collectors.toList())
                .size() > MAX_NATIONAL_IDENTITY_NUMBER_COUNT) {
            throw new BadRequestException(ERROR_MESSAGE_IDENTIFIERS_REPEATED);
        }
    }

    private boolean suppliedInputPersonNinDoesNotMatchClientOwn(Person input, RequestInfo requestInfo)
        throws BadRequestException {
        Optional<String> clientOwnPersonNin = attempt(requestInfo::getPersonNin).toOptional();
        if (clientOwnPersonNin.isPresent()) {
            return !Objects.equals(extractIdentificationNumber(input.getIdentifiers()), clientOwnPersonNin.get());
        }
        return true;
    }
}
