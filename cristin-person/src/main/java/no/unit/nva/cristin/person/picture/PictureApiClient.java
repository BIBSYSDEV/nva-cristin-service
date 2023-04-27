package no.unit.nva.cristin.person.picture;

import java.net.http.HttpClient;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.model.nva.Binary;

public class PictureApiClient extends ApiClient {

    public PictureApiClient(HttpClient client) {
        super(client);
    }

    public Void uploadPicture(String personId, Binary input) {
        return null;
    }

}
