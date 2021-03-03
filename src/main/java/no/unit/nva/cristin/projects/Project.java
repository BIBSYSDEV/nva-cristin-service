package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.ACADEMIC_DISCIPLINES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.projects.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_PROJECT_ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.HRCS_ACTIVITIES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.HRCS_CATEGORIES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.LAST_MODIFIED;
import static no.unit.nva.cristin.projects.JsonPropertyNames.MAIN_LANGUAGE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROJECT_CATEGORIES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROJECT_FUNDING_SOURCES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.RELATED_PROJECTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.START_DATE;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.TooManyFields")
public class Project {

    @JsonProperty(CRISTIN_PROJECT_ID)
    public String cristinProjectId;
    public Boolean publishable;
    public Boolean published;
    public Map<String, String> title;
    @JsonProperty(MAIN_LANGUAGE)
    public String mainLanguage;
    public String url;
    @JsonProperty(START_DATE)
    public String startDate;
    @JsonProperty(END_DATE)
    public String endDate;
    public String status;
    public Interaction created;
    @JsonProperty(LAST_MODIFIED)
    public Interaction lastModified;

    @JsonProperty(COORDINATING_INSTITUTION)
    public Organization coordinatingInstitution;
    @JsonProperty(PROJECT_FUNDING_SOURCES)
    public List<FundingSource> projectFundingSources;
    public List<Person> participants;

    @JsonProperty(PROJECT_CATEGORIES)
    public List<Category> projectCategories;
    @JsonProperty(HRCS_CATEGORIES)
    public List<Category> hrcsCategories;
    @JsonProperty(HRCS_ACTIVITIES)
    public List<Category> hrcsActivities;
    @JsonProperty(ACADEMIC_DISCIPLINES)
    public List<Category> academicDisciplines;
    public List<Category> keywords;

    @JsonProperty(ACADEMIC_SUMMARY)
    public Map<String, String> academicSummary;
    @JsonProperty(POPULAR_SCIENTIFIC_SUMMARY)
    public Map<String, String> popularScientificSummary;
    public Map<String, String> method;

    public List<String> results;

    @JsonProperty(RELATED_PROJECTS)
    public List<String> relatedProjects;

    public List<Map<String, String>> approvals;

}

