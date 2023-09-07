package no.unit.nva.client;

public interface ClientProvider<T extends GenericApiClient> {

    String VERSION = "version";
    String VERSION_ONE = "1";
    String VERSION_2023_05_26 = "2023-05-26";
    String CLIENT_WANTS_VERSION_OF_THE_API_CLIENT = "Client wants version {} of the api client";
    String CLIENT_DID_NOT_SPECIFY_VERSION_RETURNING_DEFAULT =
        "Client did not specify version. Returning default version {}";

    T getClient(String apiVersion);

}
