package no.unit.nva.cristin.organization;

import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;

import java.net.URI;

public interface HttpExecutor {

     Organization getOrganization(URI uri)
            throws NotFoundException, FailedHttpRequestException, InterruptedException;
}
