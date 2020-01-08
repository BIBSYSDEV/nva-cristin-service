package no.unit.nva.cristin.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class Project {

    private String cristin_project_id;
    private Boolean publishable;
    private Boolean published;
    private Map<String, String> title;
    private String main_language;
    private String url;
    private String start_date;
    private String end_date;
    private String status;
    private Interaction created;
    private Interaction last_modified;

    private Organization coordinating_institution;
    private List<FundingSource> project_funding_sources;
    private List<Person> participants;

    private List<Category> project_categories;
    private List<Category> hrcs_categories;
    private List<Category> hrcs_activities;
    private List<Category> academic_disciplines;
    private List<Category> keywords;

    private Map<String, String> academic_summary;
    private Map<String, String> popular_scientific_summary;
    private Map<String, String> method;

    private List<String> results;

    private List<String> related_projects;

    private List<Map<String, String>> approvals;

    String getCristin_project_id() {
        return cristin_project_id;
    }

    Boolean getPublishable() {
        return publishable;
    }

    Boolean getPublished() {
        return published;
    }

    Map<String, String> getTitle() {
        if (title == null) {
            title = new TreeMap<>();
        }
        return title;
    }

    String getMain_language() {
        return main_language;
    }

    String getUrl() {
        return url;
    }

    String getStart_date() {
        return start_date;
    }

    String getEnd_date() {
        return end_date;
    }

    String getStatus() {
        return status;
    }

    Interaction getCreated() {
        return created;
    }

    Interaction getLast_modified() {
        return last_modified;
    }

    Organization getCoordinating_institution() {
        return coordinating_institution;
    }

    List<FundingSource> getProject_funding_sources() {
        if (project_funding_sources == null) {
            project_funding_sources = new ArrayList<>();
        }
        return project_funding_sources;
    }

    List<Person> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        return participants;
    }

    List<Category> getProject_categories() {
        if (project_categories == null) {
            project_categories = new ArrayList<>();
        }
        return project_categories;
    }

    List<Category> getHrcs_categories() {
        if (hrcs_categories == null) {
            hrcs_categories = new ArrayList<>();
        }
        return hrcs_categories;
    }

    List<Category> getHrcs_activities() {
        if (hrcs_activities == null) {
            hrcs_activities = new ArrayList<>();
        }
        return hrcs_activities;
    }

    List<Category> getAcademic_disciplines() {
        if (academic_disciplines == null) {
            academic_disciplines = new ArrayList<>();
        }
        return academic_disciplines;
    }

    List<Category> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        return keywords;
    }

    Map<String, String> getAcademic_summary() {
        return academic_summary;
    }

    Map<String, String> getPopular_scientific_summary() {
        return popular_scientific_summary;
    }

    Map<String, String> getMethod() {
        return method;
    }

    List<String> getRelated_projects() {
        return related_projects;
    }

    List<String> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    List<Map<String, String>> getApprovals() {
        if (approvals == null) {
            approvals = new ArrayList<>();
        }
        return approvals;
    }
}

