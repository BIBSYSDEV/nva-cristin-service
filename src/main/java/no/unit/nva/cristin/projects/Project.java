package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.TooManyFields")
public class Project {

    @JsonProperty("cristin_project_id")
    public String cristinProjectId;
    public Boolean publishable;
    public Boolean published;
    public Map<String, String> title;
    @JsonProperty("main_language")
    public String mainLanguage;
    public String url;
    @JsonProperty("start_date")
    public String startDate;
    @JsonProperty("end_date")
    public String endDate;
    public String status;
    public Interaction created;
    @JsonProperty("last_modified")
    public Interaction lastModified;

    @JsonProperty("coordinating_institution")
    public Organization coordinatingInstitution;
    @JsonProperty("project_funding_sources")
    public List<FundingSource> projectFundingSources;
    public List<Person> participants;

    @JsonProperty("project_categories")
    public List<Category> projectCategories;
    @JsonProperty("hrcs_categories")
    public List<Category> hrcsCategories;
    @JsonProperty("hrcs_activities")
    public List<Category> hrcsActivities;
    @JsonProperty("academic_disciplines")
    public List<Category> academicDisciplines;
    public List<Category> keywords;

    @JsonProperty("academic_summary")
    public Map<String, String> academicSummary;
    @JsonProperty("popular_scientific_summary")
    public Map<String, String> popularScientificSummary;
    public Map<String, String> method;

    public List<String> results;

    @JsonProperty("related_projects")
    public List<String> relatedProjects;

    public List<Map<String, String>> approvals;

}

