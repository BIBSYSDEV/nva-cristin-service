package no.unit.nva.cristin.person.create;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.person.model.nva.Person.NATIONAL_IDENTITY_NUMBER;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
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

public class CreateCristinPersonHandler extends ApiGatewayHandler<Person, Person> {

    private static final Set<String> REQUIRED_IDENTIFIERS =
        Set.of(NATIONAL_IDENTITY_NUMBER, CristinPerson.FIRST_NAME, CristinPerson.LAST_NAME);

    private final transient CreateCristinPersonApiClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public CreateCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public CreateCristinPersonHandler(Environment environment) {
        this(new CreateCristinPersonApiClient(CristinAuthenticator.getHttpClient()), environment);
    }

    @JacocoGenerated
    public CreateCristinPersonHandler(CreateCristinPersonApiClient apiClient, Environment environment) {
        super(Person.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Person processInput(Person input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        Set<TypedValue> identifiers = extractAggregatedIdentifiers(input);

        if (hasAllRequiredIdentifiers(identifiers) && hasValidNationalIdentificationNumber(identifiers)) {
            return apiClient.createPersonInCristin(input);
        }

        throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected Integer getSuccessStatusCode(Person input, Person output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    private Set<TypedValue> extractAggregatedIdentifiers(Person input) {
        return Stream.concat(
                Optional.ofNullable(input)
                    .map(Person::getIdentifiers)
                    .orElse(Collections.emptySet()).stream(),
                Optional.ofNullable(input)
                    .map(Person::getNames)
                    .orElse(Collections.emptySet()).stream())
            .collect(Collectors.toSet());
    }

    private boolean hasAllRequiredIdentifiers(Set<TypedValue> requestIdentifiers) {
        Set<String> identifierKeys = requestIdentifiers.stream().map(TypedValue::getType).collect(Collectors.toSet());
        return identifierKeys.containsAll(REQUIRED_IDENTIFIERS);
    }

    private boolean hasValidNationalIdentificationNumber(Set<TypedValue> identifiers) {
        return verifyIdentificationNumber().test(extractIdentificationNumber(identifiers));
    }

    private Predicate<TypedValue> verifyIdentificationNumber() {
        return typedValue -> typedValue.hasData() && FodselsnummerValidator.isValid(typedValue.getValue());
    }

    private TypedValue extractIdentificationNumber(Set<TypedValue> identifiers) {
        return identifiers.stream().filter(containsIdentificationNumber()).findFirst().orElseThrow();
    }

    private Predicate<TypedValue> containsIdentificationNumber() {
        return typedValue -> NATIONAL_IDENTITY_NUMBER.equals(typedValue.getType());
    }
}
