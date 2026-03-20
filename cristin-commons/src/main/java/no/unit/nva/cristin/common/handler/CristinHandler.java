package no.unit.nva.cristin.common.handler;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

import java.util.List;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.MediaType;
import nva.commons.core.Environment;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

  public static final String DEFAULT_LANGUAGE_CODE = "nb";

  public CristinHandler(Class<I> iclass, Environment environment) {
    super(iclass, environment);
  }

  @Override
  protected List<MediaType> listSupportedMediaTypes() {
    return DEFAULT_RESPONSE_MEDIA_TYPES;
  }
}
