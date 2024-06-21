package no.unit.nva.cristin.projects.fetch;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.utils.UriUtils.PROJECT;
import java.net.http.HttpClient;
import java.util.Optional;
import no.unit.nva.cristin.model.CristinQuery;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchCristinProjectApiClient extends CristinProjectApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FetchCristinProjectApiClient.class);

    /**
     * Creates instance with default Http Client.
     */
    public FetchCristinProjectApiClient() {
        super();
    }

    public FetchCristinProjectApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @return a NvaProject filled with one transformed Cristin Project
     * @throws ApiGatewayException when there is a problem that can be returned to client
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id)
        throws ApiGatewayException {

        return Optional.of(getProject(id))
                   .filter(CristinProject::hasEnrichedContent)
                   .map(new NvaProjectBuilder())
                   .map(this::addContext)
                   .orElseThrow(() -> projectHasNotValidContent(id));
    }

    private NvaProject addContext(NvaProject nvaProject) {
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        return nvaProject;
    }

    protected CristinProject getProject(String id) throws ApiGatewayException {
        var uri = CristinQuery.fromIdentifier(id);
        var response = fetchGetResult(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(id, PROJECT), response.statusCode(), response.body());

        return getDeserializedResponse(response, CristinProject.class);
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

}
