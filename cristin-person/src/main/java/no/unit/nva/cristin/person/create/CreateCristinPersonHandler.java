package no.unit.nva.cristin.person.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.unit.nva.common.IdCreatedLogger;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.employment.create.CreatePersonEmploymentValidator;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.utils.AccessUtils;
import no.unit.nva.validation.Validator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.extractCristinInstitutionIdentifier;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static nva.commons.core.attempt.Try.attempt;

public class CreateCristinPersonHandler extends ApiGatewayHandler<Person, Person> {

    private static final Logger logger = LoggerFactory.getLogger(CreateCristinPersonHandler.class);

    public static final String ERROR_MESSAGE_IDENTIFIER_NOT_VALID =
        String.format("Required field %s is not valid", NATIONAL_IDENTITY_NUMBER);
    public static final String ERROR_MESSAGE_PAYLOAD_EMPTY = "Payload cannot be empty";
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
        validateContainsRequiredNames(extractIdentifiers(input.names()));
        validateNoDuplicateNationalIdentifiers(input.identifiers());

        if (suppliedInputPersonNinDoesNotMatchClientOwn(input, requestInfo)) {
            AccessUtils.validateIdentificationNumberAccess(requestInfo);
        }

        var identificationNumber = extractIdentificationNumber(input.identifiers()).orElse(null);
        if (nonNull(identificationNumber) && !identificationNumberIsValid(identificationNumber)) {
            throw new BadRequestException(ERROR_MESSAGE_IDENTIFIER_NOT_VALID);
        }

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        if (employmentsHasContent(input)) {
            AccessUtils.validateIdentificationNumberAccess(requestInfo);
            for (Employment employment : input.employments()) {
                CreatePersonEmploymentValidator.validate(employment);
            }
        }

        if (personNviDataHasContent(input)) {
            AccessUtils.validateIdentificationNumberAccess(requestInfo);
            Validator<PersonNvi> personNviValidator = new PersonNviValidator();
            personNviValidator.validate(input.nvi());
        }

        if (employmentsHasContent(input) || personNviDataHasContent(input)) {
            return apiClient.createPersonInCristin(input, extractCristinInstitutionIdentifier(requestInfo));
        }

        return apiClient.createPersonInCristin(input);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected void validateRequest(Person input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        // TODO: Move validation and access control to here
    }

    @Override
    protected Integer getSuccessStatusCode(Person input, Person output) {
        new IdCreatedLogger().logId(output);

        return HttpURLConnection.HTTP_CREATED;
    }

    private void validateContainsPayload(Person input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_PAYLOAD_EMPTY);
        }
    }

    private Set<String> extractIdentifiers(Set<TypedValue> typedValues) {
        return Optional.ofNullable(typedValues).orElse(Collections.emptySet()).stream().map(TypedValue::type)
            .collect(Collectors.toSet());
    }

    private void validateContainsRequiredNames(Set<String> names) throws BadRequestException {
        if (names.containsAll(REQUIRED_NAMES)) {
            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_MISSING_REQUIRED_NAMES);
    }

    private Optional<String> extractIdentificationNumber(Set<TypedValue> identifiers) {
        return identifiers.stream().filter(TypedValue::hasData)
            .filter(elm -> NATIONAL_IDENTITY_NUMBER.equals(elm.type()))
            .map(TypedValue::value).findFirst();
    }

    private boolean identificationNumberIsValid(String number) {
        return FodselsnummerValidator.isValid(number);
    }

    private void validateNoDuplicateNationalIdentifiers(Set<TypedValue> identifiers) throws BadRequestException {
        if (identifiers.stream().map(TypedValue::type)
                .filter(NATIONAL_IDENTITY_NUMBER::equals)
                .toList()
                .size() > MAX_NATIONAL_IDENTITY_NUMBER_COUNT) {
            throw new BadRequestException(ERROR_MESSAGE_IDENTIFIERS_REPEATED);
        }
    }

    private boolean suppliedInputPersonNinDoesNotMatchClientOwn(Person input, RequestInfo requestInfo) {
        var clientOwnPersonNin = attempt(requestInfo::getPersonNin).toOptional();
        if (clientOwnPersonNin.isPresent()) {
            var ninFromPayload = extractIdentificationNumber(input.identifiers()).orElse(null);
            return isNull(ninFromPayload) || !Objects.equals(ninFromPayload, clientOwnPersonNin.get());
        }
        return true;
    }

    private boolean employmentsHasContent(Person input) {
        var employments = input.employments();
        return nonNull(employments) && !employments.isEmpty();
    }

    private boolean personNviDataHasContent(Person input) {
        var personNvi = input.nvi();
        return nonNull(personNvi);
    }
}
