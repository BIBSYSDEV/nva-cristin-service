package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import java.util.function.Function;
import no.unit.nva.cristin.projects.model.cristin.CristinClinicalTrialPhaseBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinHealthProjectTypeBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.ClinicalTrialPhase;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.HealthProjectType;

public class CristinProjectToHealthProjectData implements Function<CristinProject, HealthProjectData> {

    private final transient EnumBuilder<CristinProject, ClinicalTrialPhase> clinicalTrialPhaseBuilder;
    private final transient EnumBuilder<CristinProject, HealthProjectType> healthProjectTypeBuilder;

    public CristinProjectToHealthProjectData() {
        this.clinicalTrialPhaseBuilder = new CristinClinicalTrialPhaseBuilder();
        this.healthProjectTypeBuilder = new CristinHealthProjectTypeBuilder();
    }

    @Override
    public HealthProjectData apply(CristinProject cristinProject) {
        if (isNull(cristinProject.getHealthProjectType()) && isNull(cristinProject.getClinicalTrialPhase())) {
            return null;
        }

        return new HealthProjectData(healthProjectTypeBuilder.build(cristinProject),
                                     cristinProject.getHealthProjectTypeName(),
                                     clinicalTrialPhaseBuilder.build(cristinProject));
    }

}
