package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_PROJECT_ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.MAIN_LANGUAGE;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({CRISTIN_PROJECT_ID, MAIN_LANGUAGE})
public class ProjectPresentation {

    @JsonProperty("@context")
    public String context;
    public String id;
    @JsonProperty(CRISTIN_PROJECT_ID)
    public String cristinProjectId = "";
    @JsonProperty(MAIN_LANGUAGE)
    public String mainLanguage = "";
    public List<TitlePresentation> titles = new ArrayList<>();
    public List<ParticipantPresentation> participants = new ArrayList<>();
    public List<InstitutionPresentation> institutions = new ArrayList<>();
    public List<FundingSourcePresentation> fundings = new ArrayList<>();
}
