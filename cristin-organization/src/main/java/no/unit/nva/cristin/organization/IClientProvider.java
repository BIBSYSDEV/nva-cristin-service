package no.unit.nva.cristin.organization;

public interface IClientProvider<T, R> {

    R getClient(T params);

}
