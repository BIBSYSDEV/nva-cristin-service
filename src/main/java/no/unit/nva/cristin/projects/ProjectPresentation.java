package no.unit.nva.cristin.projects;

import java.util.ArrayList;
import java.util.List;

public class ProjectPresentation {

    public String cristinProjectId = "";
    public String mainLanguage = "";
    public List<TitlePresentation> titles = new ArrayList<>();
    public List<ParticipantPresentation> participants = new ArrayList<>();
    public List<InstitutionPresentation> institutions = new ArrayList<>();
    public List<FundingSourcePresentation> fundings = new ArrayList<>();

}
