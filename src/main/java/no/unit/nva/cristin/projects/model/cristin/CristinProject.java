package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"PMD.TooManyFields"})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinProject {

    public String cristinProjectId;
    public Boolean publishable;
    public Boolean published;
    public Map<String, String> title;
    public String mainLanguage;
    public Instant startDate;
    public Instant endDate;
    public String status;
    public Map<String, Instant> created;
    public Map<String, Instant> lastModified;
    public CristinOrganization coordinatingInstitution;
    public List<FundingSource> projectFundingSources;
    public List<Person> participants;
}

