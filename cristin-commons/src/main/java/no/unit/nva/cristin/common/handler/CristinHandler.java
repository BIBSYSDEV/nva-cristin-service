package no.unit.nva.cristin.common.handler;

import com.google.common.net.MediaType;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.core.Environment;

import java.util.List;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

    public CristinHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
