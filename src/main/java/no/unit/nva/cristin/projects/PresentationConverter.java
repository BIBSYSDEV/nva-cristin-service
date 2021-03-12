package no.unit.nva.cristin.projects;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;

public class PresentationConverter {

    protected ProjectPresentation asProjectPresentation(CristinProject project, String language) {
        ProjectPresentation projectPresentation = new ProjectPresentation();
        projectPresentation.cristinProjectId = project.cristinProjectId;
        projectPresentation.mainLanguage = project.mainLanguage;

        Optional.ofNullable(project.title).orElse(new TreeMap<>() {
        }).forEach((key, value) -> {
            TitlePresentation titlePresentation = new TitlePresentation();
            titlePresentation.language = key;
            titlePresentation.title = value;
            projectPresentation.titles.add(titlePresentation);
        });

        Optional.ofNullable(project.participants).orElse(new ArrayList<>()).forEach(person -> {
            ParticipantPresentation participantPresentation = new ParticipantPresentation();
            participantPresentation.cristinPersonId = person.cristinPersonId;
            participantPresentation.fullName = person.surname + ", " + person.firstName;
            projectPresentation.participants.add(participantPresentation);
        });

        InstitutionPresentation institutionPresentation = new InstitutionPresentation();
        if (Optional.ofNullable(project.coordinatingInstitution).isPresent()) {
            institutionPresentation.cristinInstitutionId = project.coordinatingInstitution.institution
                    .cristinInstitutionId;
            institutionPresentation.name = project.coordinatingInstitution.institution.institutionName
                    .get(language);
            institutionPresentation.language = language;
            projectPresentation.institutions.add(institutionPresentation);
        }

        Optional.ofNullable(project.projectFundingSources).orElse(new ArrayList<>()).forEach(fundingSource -> {
            FundingSourcePresentation fundingSourcePresentation = new FundingSourcePresentation();
            fundingSourcePresentation.fundingSourceCode = fundingSource.fundingSourceCode;
            fundingSourcePresentation.projectCode = fundingSource.projectCode;
            fundingSourcePresentation.names = fundingSource.fundingSourceName.entrySet().stream()
                .map(name -> {
                    FundingSourceNamePresentation fundingSourceNamePresentation =
                        new FundingSourceNamePresentation();
                    fundingSourceNamePresentation.language = name.getKey();
                    fundingSourceNamePresentation.name = name.getValue();
                    return fundingSourceNamePresentation;
                })
                .collect(Collectors.toList());
            projectPresentation.fundings.add(fundingSourcePresentation);
        });

        return projectPresentation;
    }

}
