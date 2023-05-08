package no.unit.nva.cristin.organization.dto;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.model.Organization;

public class OrganizationMapper implements Function<UnitDto, Organization> {

    @Override
    public Organization apply(UnitDto unitDto) {
        return toOrganization(unitDto);
    }

    private Organization toOrganization(UnitDto unitDto) {
        return new Organization.Builder()
                   .withId(getNvaApiId(unitDto.getId(), ORGANIZATION_PATH))
                   .withName(unitDto.getUnitName())
                   .withLabels(unitDto.getUnitName())
                   .withAcronym(unitDto.getAcronym())
                   .withHasPart(unitsToOrganizations(unitDto.getSubUnits()))
                   .withPartOf(unitsToOrganizations(unitDto.getParentUnits()))
                   .build();
    }

    private Set<Organization> unitsToOrganizations(List<UnitDto> unitDto) {
        return unitDto.stream().map(this::toOrganization).collect(Collectors.toSet());
    }
}
