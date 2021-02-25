package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"cristin_project_id", "main_language"})
public class ProjectPresentation {

    @JsonProperty("cristin_project_id")
    public String cristinProjectId = "";
    @JsonProperty("main_language")
    public String mainLanguage = "";
    public List<TitlePresentation> titles = new ArrayList<>();
    public List<ParticipantPresentation> participants = new ArrayList<>();
    public List<InstitutionPresentation> institutions = new ArrayList<>();
    public List<FundingSourcePresentation> fundings = new ArrayList<>();
}
