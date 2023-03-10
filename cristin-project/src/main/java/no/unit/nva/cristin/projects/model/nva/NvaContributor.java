package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.model.CristinUnit.extractUnitIdentifier;
import static no.unit.nva.utils.ContributorRoleMapping.getCristinRole;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.model.Organization;

public class NvaContributor implements JsonSerializable {

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

    /**
     * Create a CristinPerson from identity with added roles.
     *
     * @return a CristinPerson from identity and roles
     */
    public CristinPerson toCristinPersonWithRoles() {
        CristinPerson cristinPerson = getIdentity().toCristinPersonWithoutRoles();
        cristinPerson.setRoles(extractCristinRoles());
        return cristinPerson;
    }

    private List<CristinRole> extractCristinRoles() {
        return contributorTypeCanBeMappedToCristinRole() ? getCristinRoles() : Collections.emptyList();
    }

    private boolean contributorTypeCanBeMappedToCristinRole() {
        return getCristinRole(getType()).isPresent();
    }

    private List<CristinRole> getCristinRoles() {
        var cristinRole = new CristinRole();
        addRolesBasedOnContributorsType(cristinRole);
        addOrganizationInformationToCristinRole(cristinRole);
        return List.of(cristinRole);
    }

    private void addOrganizationInformationToCristinRole(CristinRole cristinRole) {
        if (contributorHasValidUnitIdentifier()) {
            String unitIdentifier = extractUnitIdentifier(getAffiliation()).orElseThrow();
            cristinRole.setInstitutionUnit(new CristinUnit(unitIdentifier));
        } else {
            CristinInstitution defaultOrganization = fromOrganization(getAffiliation());
            cristinRole.setInstitution(defaultOrganization);
        }
    }

    private void addRolesBasedOnContributorsType(CristinRole cristinRole) {
        if (contributorTypeCanBeMappedToCristinRole()) {
            var cristinRoleCode = getCristinRole(getType()).orElseThrow();
            cristinRole.setRoleCode(cristinRoleCode);
        }
    }

    private boolean contributorHasValidUnitIdentifier() {
        return extractUnitIdentifier(getAffiliation()).isPresent();
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
