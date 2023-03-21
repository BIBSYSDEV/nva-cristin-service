package no.unit.nva.cristin.common.handler;

import com.google.common.net.MediaType;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;

import java.util.List;
import java.util.Optional;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

    public static final String DEFAULT_LANGUAGE_CODE = "nb";

    public CristinHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    /**
     * Get query parameter if present
     * @deprecated
     * This method is no longer needed. Is replaced by another.
     * <p> Use {@link RequestInfo#getQueryParameterOpt(String)} instead.
     *
     * @param queryParameter param name
     * @return param value if present
     */
    @Deprecated
    protected static Optional<String> getQueryParameter(RequestInfo requestInfo, String queryParameter) {
        return requestInfo.getQueryParameterOpt(queryParameter);
    }

}
