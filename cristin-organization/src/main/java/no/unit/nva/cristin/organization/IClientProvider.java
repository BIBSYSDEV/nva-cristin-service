package no.unit.nva.cristin.organization;

public interface IClientProvider<T> {

    T getClient(String apiVersion);

}
