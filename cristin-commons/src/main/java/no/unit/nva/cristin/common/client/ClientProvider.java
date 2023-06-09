package no.unit.nva.cristin.common.client;

public interface ClientProvider<T extends GenericApiClient> {

    T getClient(String apiVersion);

}
