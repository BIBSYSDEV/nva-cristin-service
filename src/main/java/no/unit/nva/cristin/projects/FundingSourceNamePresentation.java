package no.unit.nva.cristin.projects;

public class FundingSourceNamePresentation {

    public String name;
    public String language;

    public FundingSourceNamePresentation(String language, String name) {
        this.language = language;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
