package no.unit.nva.cristin.person.picture;

import java.net.http.HttpClient;
import no.unit.nva.cristin.common.client.ApiClient;

public class PictureApiClient extends ApiClient {

    public PictureApiClient(HttpClient client) {
        super(client);
    }

    public Void uploadPicture(String personId, byte[] input) {
        return null;
    }

}
