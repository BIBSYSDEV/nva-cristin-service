package no.unit.nva.cristin.common.client;

public interface IClientProvider<T> {

    T getClient(String apiVersion);

}
