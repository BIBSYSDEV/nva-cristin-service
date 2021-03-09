package no.unit.nva.cristin.projects;

//import static nva.commons.core.attempt.Try.attempt;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
//import nva.commons.core.JsonUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class PresentationConverter {

    //private static final Logger logger = LoggerFactory.getLogger(PresentationConverter.class);

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

        //String input = (attempt(() -> (JsonUtils.objectMapper.writeValueAsString(project))).get());
        //logger.info(input);

        //String output = (attempt(() -> (JsonUtils.objectMapper.writeValueAsString(projectPresentation))).get());
        //logger.info(output);

        return projectPresentation;
    }

}
