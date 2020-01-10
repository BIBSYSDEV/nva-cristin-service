package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.TooManyFields")
public class Project {

    @SerializedName("cristin_project_id")
    public String cristinProjectId;
    public Boolean publishable;
    public Boolean published;
    public Map<String, String> title;
    @SerializedName("main_language")
    public String mainLanguage;
    public String url;
    @SerializedName("start_date")
    public String startDate;
    @SerializedName("end_date")
    public String endDate;
    public String status;
    public Interaction created;
    @SerializedName("last_modified")
    public Interaction lastModified;

    @SerializedName("coordinating_institution")
    public Organization coordinatingInstitution;
    @SerializedName("project_funding_sources")
    public List<FundingSource> projectFundingSources;
    public List<Person> participants;

    @SerializedName("project_categories")
    public List<Category> projectCategories;
    @SerializedName("hrcs_categories")
    public List<Category> hrcsCategories;
    @SerializedName("hrcs_activities")
    public List<Category> hrcsActivities;
    @SerializedName("academic_disciplines")
    public List<Category> academicDisciplines;
    public List<Category> keywords;

    @SerializedName("academic_summary")
    public Map<String, String> academicSummary;
    @SerializedName("popular_scientific_summary")
    public Map<String, String> popularScientificSummary;
    public Map<String, String> method;

    public List<String> results;

    @SerializedName("related_projects")
    public List<String> relatedProjects;

    public List<Map<String, String>> approvals;

}

