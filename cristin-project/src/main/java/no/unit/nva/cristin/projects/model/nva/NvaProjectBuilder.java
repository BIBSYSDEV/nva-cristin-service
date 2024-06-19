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
import static no.unit.nva.utils.UriUtils.PROJECT;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.StringUtils.isNotBlank;
import static nva.commons.core.attempt.Try.attempt;

public class NvaProjectBuilder implements Function<CristinProject, NvaProject> {

    public static final String PROJECT_TYPE = "Project";

    private transient CristinProject cristinProject;
    private transient String context;

    @SuppressWarnings("unused")
    public NvaProjectBuilder() {
    }

    /**
     * Builds a NvaProject from a Cristin Project.
     */
    public NvaProjectBuilder(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
    }

    @Override
    public NvaProject apply(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
        return build();
    }

    /**
     * Build a NVA project datamodel from a Cristin project datamodel.
     *
     * @return a NvaProject converted from a CristinProject
     */
    public NvaProject build() {
        return new NvaProject.Builder()
                   .withId(getNvaApiId(cristinProject.getCristinProjectId(), PROJECT))
                   .withContext(getContext())
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

    public NvaProjectBuilder withContext(String context) {
        this.context = context;
        return this;
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
                   .filter(hasTitles -> !hasTitles.isEmpty())
                   .filter(titles -> titles.keySet().remove(cristinProject.getMainLanguage()))
                   .filter(remainingTitles -> !remainingTitles.isEmpty())
                   .map(Collections::singletonList)
                   .orElse(emptyList());
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

    private List<ExternalSource> extractExternalSources(List<CristinExternalSource> cristinExternalSources) {
        return nonNull(cristinExternalSources)
                   ? cristinExternalSources.stream()
                         .map(this::toExternalSource)
                         .collect(Collectors.toList())
                   : null;
    }

    private ExternalSource toExternalSource(CristinExternalSource cristinExternalSource) {
        return new ExternalSource(cristinExternalSource.getSourceReferenceId(),
                                  cristinExternalSource.getSourceShortName());
    }

    private URI extractWebPage(String externalUrl) {
        return attempt(() -> URI.create(externalUrl)).orElse(fail -> null);
    }

    private String getContext() {
        return context;
    }

}
