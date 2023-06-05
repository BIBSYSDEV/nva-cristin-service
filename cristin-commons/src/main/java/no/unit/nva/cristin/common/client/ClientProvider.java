package no.unit.nva.cristin.common.client;

public interface ClientProvider<T> {

    T getClient(String apiVersion);

}
