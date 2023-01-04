package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Map;

public class CristinAssocProjectForBiobank {

    public static final String CRISTIN_PROJECT_IDENTIFICATOR = "cristin_project_id";
    public static final String CRISTIN_PROJECT_ID = "url";
    public static final String CRISTIN_PROJECT_TITLE = "title";

    @JsonProperty(CRISTIN_PROJECT_ID)
    private final URI cristinProjectId;
    @JsonProperty(CRISTIN_PROJECT_IDENTIFICATOR)
    private final String cristinProjectIdentificator;

    @JsonProperty(CRISTIN_PROJECT_TITLE)
    private final Map<String, String> title;


    /**
     * Constructor
     * @param cristinProjectId - URI
     * @param cristinProjectIdentificator - code
     * @param title - name of the project
     */
    public CristinAssocProjectForBiobank(URI cristinProjectId,
                                         String cristinProjectIdentificator,
                                         Map<String, String> title) {
        this.cristinProjectId = cristinProjectId;
        this.cristinProjectIdentificator = cristinProjectIdentificator;
        this.title = title;
    }

    public URI getCristinProjectId() {
        return cristinProjectId;
    }

    public String getCristinProjectIdentificator() {
        return cristinProjectIdentificator;
    }

    public Map<String, String> getTitle() {
        return title;
    }
}
