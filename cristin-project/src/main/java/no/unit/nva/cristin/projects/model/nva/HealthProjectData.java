package no.unit.nva.cristin.projects.model.nva;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

public class HealthProjectData implements JsonSerializable {

    public static final String LABEL = "label";
    public static final String CLINICAL_TRIAL_PHASE = "clinicalTrialPhase";

    @JsonProperty(TYPE)
    private final HealthProjectType type;
    @JsonProperty(LABEL)
    private final Map<String, String> label;
    @JsonProperty(CLINICAL_TRIAL_PHASE)
    private final ClinicalTrialPhase clinicalTrialPhase;

    /**
     * Nva model for a project having additional health related data.
     */
    @JsonCreator
    public HealthProjectData(@JsonProperty(TYPE) HealthProjectType type, @JsonProperty(LABEL) Map<String, String> label,
                             @JsonProperty(CLINICAL_TRIAL_PHASE) ClinicalTrialPhase clinicalTrialPhase) {
        this.type = type;
        this.label = nonNull(label) ? Collections.unmodifiableMap(label) : Collections.emptyMap();
        this.clinicalTrialPhase = clinicalTrialPhase;
    }

    public HealthProjectType getType() {
        return type;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public ClinicalTrialPhase getClinicalTrialPhase() {
        return clinicalTrialPhase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HealthProjectData that)) {
            return false;
        }
        return Objects.equals(getType(), that.getType())
               && Objects.equals(getLabel(), that.getLabel())
               && Objects.equals(getClinicalTrialPhase(), that.getClinicalTrialPhase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getLabel(), getClinicalTrialPhase());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
