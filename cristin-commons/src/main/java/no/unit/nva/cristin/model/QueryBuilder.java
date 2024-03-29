package no.unit.nva.cristin.model;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.invalidPathParameterMessage;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.common.ErrorMessages.requiredMissingMessage;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds Cristin query parameters using builder methods and NVA input parameters.
 */
public abstract class QueryBuilder<T extends Enum<T> & IParameterKey> {

    protected static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
    protected static final String PARAMETER_PAGE_DEFAULT_VALUE = "1";
    protected static final String PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    protected final transient Set<String> invalidKeys = new HashSet<>(0);
    protected final transient CristinQuery<T> query;
    protected transient boolean notValidated = true;

    /**
     * Constructor of CristinQuery.Builder.
     * <p>Usage:</p>
     * <samp>new CristinQuery.Builder()<br>
     * .fromRequestInfo(requestInfo)<br>
     * .withRequiredParameters(IDENTITY,PAGE_CURRENT,PAGE_ITEMS_PER_PAGE)<br>
     * .build()
     * </samp>
     */
    public QueryBuilder(CristinQuery<T> query) {
        this.query = query;
    }

    /**
     * Builder of CristinQuery.
     * @throws BadRequestException if parameters are invalid or missing
     */
    public CristinQuery<T>  build() throws BadRequestException {
        if (notValidated) {
            validate();
        }
        return query;
    }

    /**
     * Validator of CristinQuery.Builder.
     * @throws BadRequestException if parameters are invalid or missing
     */
    public QueryBuilder<T> validate() throws BadRequestException {
        assignDefaultValues();
        for (var entry : query.pathParameters.entrySet()) {
            throwInvalidPathValue(entry);
        }
        for (var entry : query.queryParameters.entrySet()) {
            throwInvalidParamererValue(entry);
        }
        for (var entry : query.facetParameters.entrySet()) {
            throwInvalidParamererValue(entry);
        }
        if (!requiredMissing().isEmpty()) {
            throw new BadRequestException(requiredMissingMessage(getMissingKeys()));
        }
        if (!invalidKeys.isEmpty()) {
            throw new BadRequestException(validQueryParameterNamesMessage(validKeys()));
        }
        notValidated = false;
        return this;
    }

    /**
     * Adds query and path parameters from requestInfo.
     */
    public final QueryBuilder<T> fromRequestInfo(RequestInfo requestInfo) {
        return fromPathParameters(requestInfo.getPathParameters())
                   .fromQueryParameters(requestInfo.getQueryParameters());
    }

    /**
     * Adds parameters from path.
     * */
    public QueryBuilder<T> fromPathParameters(Map<String, String> parameters) {
        parameters.forEach(this::setPath);
        return this;
    }

    /**
     * Adds parameters from query.
     */
    public QueryBuilder<T> fromQueryParameters(Map<String, String> parameters) {
        parameters.forEach(this::setValue);
        return this;
    }


    /**
     * Defines which parameters are required.
     * @param requiredParameters comma seperated QueryParameterKeys
     */
    @SafeVarargs
    public final QueryBuilder<T> withRequiredParameters(T... requiredParameters) {
        var tmpSet = Set.of(requiredParameters);
        query.otherRequiredKeys.addAll(tmpSet);
        return this;
    }

    /**
     * Sample code for assignDefaultValues.
     * <p>Usage:</p>
     * <samp>requiredMissing().forEach(key -> { <br>
     *     switch (key) {<br>
     *         case LANGUAGE:<br>
     *             query.setValue(key, DEFAULT_LANGUAGE_CODE);<br>
     *             break;<br>
     *         default:<br>
     *             break;<br>
     *     }});<br>
     * </samp>
     */
    protected abstract void assignDefaultValues();

    /**
     * Sample code for setPath.
     * <p>Usage:</p>
     * <samp>var nonNullValue = nonNull(value) ? value : EMPTY_STRING;<br>
     * if (key.equals(PATH_IDENTITY.getNvaKey())) {<br>
     *     withPathIdentity(nonNullValue);<br>
     * } else if (key.equals(PATH_PROJECT.getNvaKey()) || key.equals(PATH_PROJECT.getKey())) {<br>
     *     withPathProject(nonNullValue);<br>
     * } else {<br>
     *     invalidKeys.add(key);<br>
     * }<br>
     * </samp>
     */
    protected abstract void setPath(String key, String value);

    /**
     * Sample code for setValue.
     * <p>Usage:</p>
     * <samp>var qpKey = keyFromString(key,value);<br>
     * if(qpKey.equals(INVALID)) {<br>
     *     invalidKeys.add(key);<br>
     * } else {<br>
     *     query.setValue(qpKey, value);<br>
     * }<br>
     * </samp>
     */
    protected abstract void setValue(String key, String value);

    /**
        returns T.VALID_QUERY_PARAMETER_NVA_KEYS
     */
    protected abstract Set<String> validKeys();

    protected boolean invalidQueryParameter(T key, String value) {
        return isNull(value) || !value.matches(key.getPattern());
    }

    protected boolean invalidPathParameter(T key, String value) {
        return !(value.isBlank() || value.matches(key.getPattern()));
    }

    protected Set<String> getMissingKeys() {
        return
            requiredMissing()
                .stream()
                .map(IParameterKey::getNvaKey)
                .collect(Collectors.toSet());
    }

    protected Set<T> required() {
        return
            Stream.concat(
                    query.otherRequiredKeys.stream(),
                    query.pathParameters.keySet().stream())
                .collect(Collectors.toSet());
    }

    protected Set<T> requiredMissing() {
        return
            required().stream()
                .filter(key -> !query.queryParameters.containsKey(key))
                .filter(key -> !query.pathParameters.containsKey(key))
                .collect(Collectors.toSet());
    }

    protected void throwInvalidParamererValue(Entry<T, String> entry) throws BadRequestException {
        final var key = entry.getKey();
        if (invalidQueryParameter(key, entry.getValue())) {
            final var keyName = key.getNvaKey();
            String errorMessage;
            if (nonNull(key.getErrorMessage())) {
                errorMessage = String.format(key.getErrorMessage(), keyName);
            } else {
                errorMessage = invalidQueryParametersMessage(keyName, EMPTY_STRING);
            }
            logger.info("INVALID PARAMETER VALUE [" + entry.getValue() + "]");
            throw new BadRequestException(errorMessage);
        }
    }

    protected void throwInvalidPathValue(Entry<T, String> entry) throws BadRequestException {
        final var key = entry.getKey();
        if (invalidPathParameter(key, entry.getValue())) {
            final var keyName = key.getNvaKey();
            final var errorMessage =
                nonNull(key.getErrorMessage())
                    ? key.getErrorMessage()
                    : invalidPathParameterMessage(keyName);
            logger.info("INVALID PATH VALUE [" + entry.getValue() + "]");

            throw new BadRequestException(errorMessage);
        }
    }
}