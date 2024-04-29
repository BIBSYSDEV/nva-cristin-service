package no.unit.nva.common;

import static no.unit.nva.common.LogMessages.CLIENT_CREATED_RESOURCE_TEMPLATE;
import static no.unit.nva.common.LogMessages.COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Optional;
import no.unit.nva.model.UriId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdLogger {

    private static final Logger logger = LoggerFactory.getLogger(IdLogger.class);

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
