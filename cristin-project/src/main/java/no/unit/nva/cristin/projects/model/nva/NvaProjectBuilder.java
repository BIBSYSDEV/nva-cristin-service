package no.unit.nva.cristin.projects.model.nva;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinApplicationCodeBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalAuthorityBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalStatusBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinClinicalTrialPhaseBuilder;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinContactInfo;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingAmount;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinHealthProjectTypeBuilder;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.cristin.CristinTypedLabel;
import no.unit.nva.model.ApprovalStatus;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.model.Organization;
import no.unit.nva.model.DateInfo;
import no.unit.nva.model.TypedLabel;
import nva.commons.core.language.LanguageMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nva.commons.core.paths.UriWrapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.projects.model.nva.Funding.UNCONFIRMED_FUNDING;
import static no.unit.nva.utils.ContributorRoleMapping.getNvaRole;
import static no.unit.nva.utils.UriUtils.PROJECT;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.StringUtils.isNotBlank;
import static nva.commons.core.attempt.Try.attempt;

public class NvaProjectBuilder {

    public static final String PROJECT_TYPE = "Project";

    public static final String FUNDING_SOURCES = "funding-sources";
    public static final String PRO_CREATOR = "PRO_CREATOR";

    private final transient CristinProject cristinProject;
    private transient String context;

    private final transient EnumBuilder<CristinProject, ClinicalTrialPhase> clinicalTrialPhaseBuilder;
    private final transient EnumBuilder<CristinProject, HealthProjectType> healthProjectTypeBuilder;
    private final transient EnumBuilder<CristinApproval, ApprovalAuthority> approvalAuthorityBuilder;
    private final transient EnumBuilder<CristinApproval, ApplicationCode> applicationCodeBuilder;
    private final transient EnumBuilder<CristinApproval, ApprovalStatus> approvalStatusBuilder;

    /**
     * Builds a NvaProject from a Cristin Project.
     */
    public NvaProjectBuilder(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
        this.clinicalTrialPhaseBuilder = new CristinClinicalTrialPhaseBuilder();
        this.healthProjectTypeBuilder = new CristinHealthProjectTypeBuilder();
        this.approvalAuthorityBuilder = new CristinApprovalAuthorityBuilder();
        this.applicationCodeBuilder = new CristinApplicationCodeBuilder();
        this.approvalStatusBuilder = new CristinApprovalStatusBuilder();
    }

    private static List<NvaContributor> transformCristinPersonsToNvaContributors(List<CristinPerson> participants) {
        return participants.stream()
                .flatMap(NvaProjectBuilder::generateRoleBasedContribution)
                .collect(Collectors.toList());
    }

    private static Stream<NvaContributor> generateRoleBasedContribution(CristinPerson cristinPerson) {
        return cristinPerson.getRoles().stream()
                .map(role -> createNvaContributorFromCristinPersonByRole(cristinPerson, role));
    }

    private static NvaContributor createNvaContributorFromCristinPersonByRole(CristinPerson cristinPerson,
                                                                              CristinRole role) {
        NvaContributor nvaContributor = new NvaContributor();
        if (getNvaRole(role.getRoleCode()).isPresent()) {
            nvaContributor.setType(getNvaRole(role.getRoleCode()).get());
        }
        nvaContributor.setIdentity(Person.fromCristinPerson(cristinPerson));
        nvaContributor.setAffiliation(extractDepartmentOrFallbackToInstitutionForUserRole(role));
        return nvaContributor;
    }

    private static Organization extractDepartmentOrFallbackToInstitutionForUserRole(CristinRole role) {
        Optional<Organization> unitAffiliation = Optional.ofNullable(role.getInstitutionUnit())
            .map(CristinUnit::toOrganization);
        Optional<Organization> institutionAffiliation = Optional.ofNullable(role.getInstitution())
            .map(CristinInstitution::toOrganization);

        return unitAffiliation.orElse(institutionAffiliation.orElse(null));
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
                   .withNewFunding(extractFunding())
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
                   .withHealthProjectData(extractHealthProjectData(cristinProject))
                   .withApprovals(extractApprovals(cristinProject.getApprovals()))
                   .withExemptFromPublicDisclosure(cristinProject.getExemptFromPublicDisclosure())
                   .withCreator(extractCreator(cristinProject.getCreator()))
                   .withWebPage(extractWebPage(cristinProject.getExternalUrl()))
                   .build();
    }

    private URI extractWebPage(String externalUrl) {
        return attempt(() -> URI.create(externalUrl)).orElse(fail -> null);
    }

    private NvaContributor extractCreator(CristinPerson creator) {
        return Optional.ofNullable(creator)
            .stream().peek(this::addRoleDataIfMissing)
            .flatMap(NvaProjectBuilder::generateRoleBasedContribution)
                   .findAny()
                   .or(() -> creatorWithoutAffiliation(creator))
                   .orElse(null);
    }

    private Optional<NvaContributor> creatorWithoutAffiliation(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .filter(presentCreator -> nonNull(presentCreator.getCristinPersonId()))
                   .map(presentCreator -> {
                       var creatorWithoutAffiliation = new NvaContributor();
                       creatorWithoutAffiliation.setIdentity(Person.fromCristinPerson(presentCreator));
                       return creatorWithoutAffiliation;
                   });
    }

    private void addRoleDataIfMissing(CristinPerson cristinPerson) {
        if (isNull(cristinPerson.getRoles())) {
            cristinPerson.setRoles(emptyList());
        } else {
            cristinPerson.getRoles().forEach(role -> role.setRoleCode(PRO_CREATOR));
        }
    }

    private List<Approval> extractApprovals(List<CristinApproval> cristinApprovals) {
        return cristinApprovals.stream().map(this::toApproval).collect(Collectors.toList());
    }

    private Approval toApproval(CristinApproval cristinApproval) {
        var authority = approvalAuthorityBuilder.build(cristinApproval);
        var applicationCode = applicationCodeBuilder.build(cristinApproval);
        var status = approvalStatusBuilder.build(cristinApproval);

        return new Approval(cristinApproval.getApprovedDate(), authority, status, applicationCode,
                            cristinApproval.getApprovalReferenceId(), cristinApproval.getApprovedByName());
    }

    private HealthProjectData extractHealthProjectData(CristinProject cristinProject) {
        if (isNull(cristinProject.getHealthProjectType()) && isNull(cristinProject.getClinicalTrialPhase())) {
            return null;
        }

        return new HealthProjectData(healthProjectTypeBuilder.build(cristinProject),
                                     cristinProject.getHealthProjectTypeName(),
                                     clinicalTrialPhaseBuilder.build(cristinProject));
    }

    private List<Organization> extractInstitutionsResponsibleForResearch(List<CristinOrganization>
                                                                             institutionsResponsibleForResearch) {
        return institutionsResponsibleForResearch.stream()
                   .map(CristinOrganization::extractPreferredTypeOfOrganization)
                   .collect(Collectors.toList());
    }

    private List<URI> extractRelatedProjects(List<String> cristinRelatedProjects) {
        return nonNull(cristinRelatedProjects)
                   ? cristinRelatedProjects.stream()
                         .map(NvaProjectBuilder::cristinUriStringWithIdentifierToNvaUri)
                         .collect(Collectors.toList())
                   : null;
    }

    private static URI cristinUriStringWithIdentifierToNvaUri(String cristinUri) {
        var identifier = extractLastPathElement(UriWrapper.fromUri(cristinUri).getUri());
        return getNvaApiId(identifier, PROJECT);
    }

    private List<ExternalSource> extractExternalSources(List<CristinExternalSource> cristinExternalSources) {
        return nonNull(cristinExternalSources)
                   ? cristinExternalSources.stream()
                         .map(NvaProjectBuilder::toExternalSource)
                         .collect(Collectors.toList())
                   : null;
    }

    private static ExternalSource toExternalSource(CristinExternalSource cristinExternalSource) {
        return new ExternalSource(cristinExternalSource.getSourceReferenceId(),
                                  cristinExternalSource.getSourceShortName());
    }

    private List<TypedLabel> extractTypedLabels(List<CristinTypedLabel> cristinTypedLabels) {
        return nonNull(cristinTypedLabels)
                   ? cristinTypedLabels.stream().map(NvaProjectBuilder::toTypedLabel).collect(Collectors.toList())
                   : null;
    }

    private static TypedLabel toTypedLabel(CristinTypedLabel cristinTypedLabel) {
        return new TypedLabel(cristinTypedLabel.getCode(), cristinTypedLabel.getName());
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

    private DateInfo extractDateInfo(CristinDateInfo cristinDateInfo) {
        return nonNull(cristinDateInfo) ? new DateInfo(cristinDateInfo.getSourceShortName(),
                                                       cristinDateInfo.getDate()) : null;
    }

    private String getContext() {
        return context;
    }

    private List<Map<String, String>> createCristinIdentifier() {
        return nonNull(cristinProject.getCristinProjectId())
                ? singletonList(Map.of(
                    Constants.TYPE, Constants.CRISTIN_IDENTIFIER_TYPE,
                    Constants.VALUE, cristinProject.getCristinProjectId()))
                : emptyList();
    }

    private Organization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.getCoordinatingInstitution())
            .map(CristinOrganization::extractPreferredTypeOfOrganization).orElse(null);
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

    private List<NvaContributor> extractContributors() {
        return Optional.ofNullable(cristinProject.getParticipants())
                .map(NvaProjectBuilder::transformCristinPersonsToNvaContributors)
                .orElse(emptyList());
    }

    private List<Funding> extractFunding() {
        return cristinProject.getProjectFundingSources().stream()
                .map(this::createFunding)
                .collect(Collectors.toList());
    }

    private Funding createFunding(CristinFundingSource cristinFunding) {
        var source = extractFundingSource(cristinFunding);
        var identifier = cristinFunding.getProjectCode();
        var labels = cristinFunding.getFundingSourceName();

        return new Funding(UNCONFIRMED_FUNDING, source, identifier, labels);
    }

    private URI extractFundingSource(CristinFundingSource cristinFunding) {
        var urlEncodedSourceIdentifier = URLEncoder.encode(cristinFunding.getFundingSourceCode(),
                                                           StandardCharsets.UTF_8);
        var uriString = getNvaApiUri(FUNDING_SOURCES).toString();
        return URI.create(uriString + "/" + urlEncodedSourceIdentifier);
    }

    public NvaProjectBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    private ProjectStatus extractProjectStatus() {
        return ProjectStatus.fromCristinStatus(cristinProject.getStatus());
    }
}
