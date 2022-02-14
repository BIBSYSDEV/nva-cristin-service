package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTITY;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.ContributorRoleMapping.getCristinRole;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.projects.model.cristin.CristinUnit;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;

@SuppressWarnings("unused")
@JsonPropertyOrder({TYPE, IDENTITY, AFFILIATION})
public class NvaContributor {

    private static final Pattern UNIT_ID_PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

    private String type;
    private Person identity;
    private Organization affiliation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Person getIdentity() {
        return identity;
    }

    public void setIdentity(Person identity) {
        this.identity = identity;
    }

    public Organization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Organization affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getIdentity(), getAffiliation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaContributor)) {
            return false;
        }
        NvaContributor that = (NvaContributor) o;
        return getType().equals(that.getType())
               && getIdentity().equals(that.getIdentity())
               && getAffiliation().equals(that.getAffiliation());
    }

    public CristinPerson toCristinPersonWithRoles() {
        CristinPerson cristinPerson = getIdentity().toCristinPersonWithoutRoles();
        cristinPerson.setRoles(extractCristinRoles());
        return cristinPerson;
    }



    private List<CristinRole> extractCristinRoles() {
        return contributorTypeCanBeMappedToCristinRole()
                   ? getCristinRoles()
                   : Collections.emptyList();
    }

    private boolean contributorTypeCanBeMappedToCristinRole() {
        return getCristinRole(getType()).isPresent();
    }

    private List<CristinRole> extractCristinRoles(NvaContributor contributor) {
        return contributorTypeCanBeMappedToCristinRole()
                   ? getCristinRoles()
                   : Collections.emptyList();
    }

    private List<CristinRole> getCristinRoles() {
        var cristinRole = new CristinRole();
        addRolesBasedOnContributorsType(cristinRole);
        addOrganizationInformationToCristinRole(cristinRole);
        return List.of(cristinRole);
    }

    private void addOrganizationInformationToCristinRole(CristinRole cristinRole) {
        if (contributorHasValidUnitIdentifier()) {
            CristinUnit institutionUnit = toCristinUnit(extractUnitIdentifier().orElseThrow());
            cristinRole.setInstitutionUnit(institutionUnit);
        } else {
            CristinInstitution defaultOrganization = fromOrganization(getAffiliation());
            cristinRole.setInstitution(defaultOrganization);
        }
    }

    private CristinRole addRolesBasedOnContributorsType(CristinRole cristinRole) {
        if (contributorTypeCanBeMappedToCristinRole()) {
            var cristinRoleCode = getCristinRole(getType()).orElseThrow();
            cristinRole.setRoleCode(cristinRoleCode);
        }
        return cristinRole;
    }

    private boolean contributorHasValidUnitIdentifier() {
        return extractUnitIdentifier().isPresent();
    }

    private CristinUnit toCristinUnit(String unitIdentifier) {
        CristinUnit cristinUnit = new CristinUnit();
        cristinUnit.setCristinUnitId(unitIdentifier);
        return cristinUnit;
    }

    private Optional<String> extractUnitIdentifier() {
        return Optional.of(getAffiliation())
            .map(Organization::getId).map(UriUtils::extractLastPathElement)
            .filter(identifier -> UNIT_ID_PATTERN.matcher(identifier).matches());
    }

}
