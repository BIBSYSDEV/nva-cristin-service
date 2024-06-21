package no.unit.nva.cristin.projects.model.nva;

import java.net.URI;
import java.util.function.Function;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinContactInfo;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingAmount;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinApprovalToApproval;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinExternalSourcesToExternalSources;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinFundingSourceToFunding;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinPersonsToNvaContributors;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinProjectCreatorToNvaContributor;
import no.unit.nva.cristin.projects.model.cristin.adapter.CristinProjectToHealthProjectData;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.model.Organization;
import no.unit.nva.model.DateInfo;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.adapter.CristinTypedLabelToNvaFormat;
import nva.commons.core.language.LanguageMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import nva.commons.core.paths.UriWrapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.projects.model.nva.NvaProject.PROJECT_TYPE;
import static no.unit.nva.utils.UriUtils.PROJECT;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.StringUtils.isNotBlank;
import static nva.commons.core.attempt.Try.attempt;

public class NvaProjectBuilder implements Function<CristinProject, NvaProject> {

    private transient CristinProject cristinProject;

    @Override
    public NvaProject apply(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
        return build();
    }

    private NvaProject build() {
        return new NvaProject.Builder()
                   .withId(getNvaApiId(cristinProject.getCristinProjectId(), PROJECT))
                   .withType(PROJECT_TYPE)
                   .withIdentifiers(createCristinIdentifier())
                   .withTitle(extractMainTitle())
                   .withAlternativeTitles(extractAlternativeTitles())
                   .withLanguage(LanguageMapper.toUri(cristinProject.getMainLanguage()))
                   .withStartDate(cristinProject.getStartDate())
                   .withEndDate(cristinProject.getEndDate())
                   .withFunding(extractFunding())
                   .withCoordinatingInstitution(extractCoordinatingInstitution())
                   .withContributors(extractContributors())
                   .withStatus(extractProjectStatus())
                   .withAcademicSummary(cristinProject.getAcademicSummary())
                   .withPopularScientificSummary(cristinProject.getPopularScientificSummary())
                   .withPublished(cristinProject.getPublished())
                   .withPublishable(cristinProject.getPublishable())
                   .withCreated(extractDateInfo(cristinProject.getCreated()))
                   .withLastModified(extractDateInfo(cristinProject.getLastModified()))
                   .withContactInfo(extractContactInfo(cristinProject.getContactInfo()))
                   .withFundingAmount(extractFundingAmount(cristinProject.getTotalFundingAmount()))
                   .withMethod(cristinProject.getMethod())
                   .withEquipment(cristinProject.getEquipment())
                   .withProjectCategories(extractTypedLabels(cristinProject.getProjectCategories()))
                   .withKeywords(extractTypedLabels(cristinProject.getKeywords()))
                   .withExternalSources(extractExternalSources(cristinProject.getExternalSources()))
                   .withRelatedProjects(extractRelatedProjects(cristinProject.getRelatedProjects()))
                   .withInstitutionsResponsibleForResearch(
                       extractInstitutionsResponsibleForResearch(
                           cristinProject.getInstitutionsResponsibleForResearch()))
                   .withHealthProjectData(new CristinProjectToHealthProjectData().apply(cristinProject))
                   .withApprovals(extractApprovals(cristinProject.getApprovals()))
                   .withExemptFromPublicDisclosure(cristinProject.getExemptFromPublicDisclosure())
                   .withCreator(new CristinProjectCreatorToNvaContributor().apply(cristinProject.getCreator()))
                   .withWebPage(extractWebPage(cristinProject.getExternalUrl()))
                   .build();
    }

    private List<Map<String, String>> createCristinIdentifier() {
        return nonNull(cristinProject.getCristinProjectId())
                   ? singletonList(Map.of(
                       Constants.TYPE, Constants.CRISTIN_IDENTIFIER_TYPE,
                       Constants.VALUE, cristinProject.getCristinProjectId()))
                   : emptyList();
    }

    private String extractMainTitle() {
        return Optional.ofNullable(cristinProject.getTitle())
                   .filter(hasTitles -> isNotBlank(cristinProject.getMainLanguage()))
                   .map(titles -> titles.get(cristinProject.getMainLanguage()))
                   .orElse(null);
    }

    private List<Map<String, String>> extractAlternativeTitles() {
        return Optional.ofNullable(cristinProject.getTitle())
                   .map(this::removeMainTitle)
                   .filter(this::hasData)
                   .map(Collections::singletonList)
                   .orElse(emptyList());
    }

    private Map<String, String> removeMainTitle(Map<String, String> titles) {
        titles.remove(cristinProject.getMainLanguage());
        return titles;
    }

    private boolean hasData(Map<String, String> titles) {
        return !titles.isEmpty();
    }

    private List<Funding> extractFunding() {
        return cristinProject.getProjectFundingSources().stream()
                   .map(new CristinFundingSourceToFunding())
                   .collect(Collectors.toList());
    }

    private Organization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.getCoordinatingInstitution())
                   .map(CristinOrganization::extractPreferredTypeOfOrganization)
                   .orElse(null);
    }

    private List<NvaContributor> extractContributors() {
        return Optional.ofNullable(cristinProject.getParticipants())
                   .map(new CristinPersonsToNvaContributors())
                   .orElse(emptyList());
    }

    private ProjectStatus extractProjectStatus() {
        return attempt(() ->  ProjectStatus.fromCristinStatus(cristinProject.getStatus()))
                   .orElse(fail -> null);
    }

    private DateInfo extractDateInfo(CristinDateInfo cristinDateInfo) {
        return nonNull(cristinDateInfo) ? new DateInfo(cristinDateInfo.getSourceShortName(),
                                                       cristinDateInfo.getDate()) : null;
    }

    private ContactInfo extractContactInfo(CristinContactInfo cristinContactInfo) {
        return nonNull(cristinContactInfo) ? new ContactInfo(cristinContactInfo.getContactPerson(),
                                                             cristinContactInfo.getInstitution(),
                                                             cristinContactInfo.getEmail(),
                                                             cristinContactInfo.getPhone()) : null;
    }

    private FundingAmount extractFundingAmount(CristinFundingAmount cristinFundingAmount) {
        return nonNull(cristinFundingAmount) ? new FundingAmount(cristinFundingAmount.getCurrencyCode(),
                                                                 cristinFundingAmount.getAmount()) : null;
    }

    private List<TypedLabel> extractTypedLabels(List<CristinTypedLabel> cristinTypedLabels) {
        return nonNull(cristinTypedLabels)
                   ? cristinTypedLabels.stream()
                         .map(new CristinTypedLabelToNvaFormat())
                         .collect(Collectors.toList())
                   : null;
    }

    private List<URI> extractRelatedProjects(List<String> cristinRelatedProjects) {
        return nonNull(cristinRelatedProjects)
                   ? cristinRelatedProjects.stream()
                         .map(this::cristinUriStringWithIdentifierToNvaUri)
                         .collect(Collectors.toList())
                   : null;
    }

    private URI cristinUriStringWithIdentifierToNvaUri(String cristinUri) {
        var identifier = extractLastPathElement(UriWrapper.fromUri(cristinUri).getUri());
        return getNvaApiId(identifier, PROJECT);
    }

    private List<Approval> extractApprovals(List<CristinApproval> cristinApprovals) {
        return cristinApprovals.stream()
                   .map(new CristinApprovalToApproval())
                   .collect(Collectors.toList());
    }

    private List<Organization> extractInstitutionsResponsibleForResearch(List<CristinOrganization>
                                                                             institutionsResponsibleForResearch) {
        return institutionsResponsibleForResearch.stream()
                   .map(CristinOrganization::extractPreferredTypeOfOrganization)
                   .collect(Collectors.toList());
    }

    private List<ExternalSource> extractExternalSources(List<CristinExternalSource> externalSources) {
        return new CristinExternalSourcesToExternalSources().apply(externalSources);
    }

    private URI extractWebPage(String externalUrl) {
        return attempt(() -> URI.create(externalUrl)).orElse(fail -> null);
    }

}
