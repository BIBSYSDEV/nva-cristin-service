package no.unit.nva.cristin.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Project {

    String cristin_project_id;
    Boolean publishable;
    Boolean published;
    Map<String, String> title;
    String main_language;
    String url;
    String start_date;
    String end_date;
    String status;
    Interaction created;
    Interaction last_modified;

    Organization coordinating_institution;
    List<FundingSource> funding_sources;
    List<Person> participants;

    List<Category> project_categories;
    List<Category> hrcs_categories;
    List<Category> hrcs_activities;
    List<Category> academic_disciplines;
    List<Category> keywords;

    Map<String, String> academic_summary;
    Map<String, String> popular_scientific_summary;
    Map<String, String> method;

    List<String> results;

    List<String> related_projects;

    List<Map<String, String>> approvals;

    public String getCristin_project_id() {
        return cristin_project_id;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public Boolean getPublished() {
        return published;
    }

    public Map<String, String> getTitle() {
        if (title == null) {
            title = new TreeMap<>();
        }
        return title;
    }

    public String getMain_language() {
        return main_language;
    }

    public String getUrl() {
        return url;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public String getStatus() {
        return status;
    }

    public Interaction getCreated() {
        return created;
    }

    public Interaction getLast_modified() {
        return last_modified;
    }

    public Organization getCoordinating_institution() {
        return coordinating_institution;
    }

    public List<FundingSource> getFunding_sources() {
        if (funding_sources == null) {
            funding_sources = new ArrayList<>();
        }
        return funding_sources;
    }

    public List<Person> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        return participants;
    }

    public List<Category> getProject_categories() {
        if (project_categories == null) {
            project_categories = new ArrayList<>();
        }
        return project_categories;
    }

    public List<Category> getHrcs_categories() {
        if (hrcs_categories == null) {
            hrcs_categories = new ArrayList<>();
        }
        return hrcs_categories;
    }

    public List<Category> getHrcs_activities() {
        if (hrcs_activities == null) {
            hrcs_activities = new ArrayList<>();
        }
        return hrcs_activities;
    }

    public List<Category> getAcademic_disciplines() {
        if (academic_disciplines == null) {
            academic_disciplines = new ArrayList<>();
        }
        return academic_disciplines;
    }

    public List<Category> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        return keywords;
    }

    public Map<String, String> getAcademic_summary() {
        return academic_summary;
    }

    public Map<String, String> getPopular_scientific_summary() {
        return popular_scientific_summary;
    }

    public Map<String, String> getMethod() {
        return method;
    }

    public List<String> getRelated_projects() {
        return related_projects;
    }

    public List<String> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    public List<Map<String, String>> getApprovals() {
        if (approvals == null) {
            approvals = new ArrayList<>();
        }
        return approvals;
    }
}

