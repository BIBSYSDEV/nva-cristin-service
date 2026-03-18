package no.unit.nva.cristin.person.institution.common;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

import java.util.List;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.MediaType;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public abstract class PersonInstitutionInfoHandler<I, O> extends ApiGatewayHandler<I, O> {

  public PersonInstitutionInfoHandler(Class<I> iclass, Environment environment) {
    super(iclass, environment);
  }

  @Override
  protected List<MediaType> listSupportedMediaTypes() {
    return DEFAULT_RESPONSE_MEDIA_TYPES;
  }

  protected void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
    if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
      throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP);
    }
  }
}
