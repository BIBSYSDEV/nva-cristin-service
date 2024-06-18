package no.unit.nva.common;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Optional;
import no.unit.nva.model.UriId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdCreatedLogger {

    private static final Logger logger = LoggerFactory.getLogger(IdCreatedLogger.class);

    public static final String COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE =
        "Could not extract identifier of newly created resource";
    public static final String CLIENT_CREATED_RESOURCE_TEMPLATE = "Client created resource: %s";

    public void logId(UriId uriId) {
        attempt(() -> logCreatedIdentifier(uriId)).orElse(fail -> logCreatedError());
    }

    private Object logCreatedIdentifier(UriId uriId) {
        var identifier = extractCreatedIdentifier(uriId);
        logger.info(String.format(CLIENT_CREATED_RESOURCE_TEMPLATE, identifier));

        return null;
    }

    private URI extractCreatedIdentifier(UriId uriId) {
        return Optional.ofNullable(uriId).map(UriId::getId).orElse(null);
    }

    private Object logCreatedError() {
        logger.warn(COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE);
        return null;
    }

}
