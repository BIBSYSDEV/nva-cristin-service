package no.unit.nva.cristin.organization.dto.version20230510.mapper;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.organization.dto.version20230510.UnitDto;
import no.unit.nva.model.Organization;

public class OrganizationFromUnitMapper implements Function<UnitDto, Organization> {

    @Override
    public Organization apply(UnitDto unitDto) {
        return nonNull(unitDto) ? toOrganization(unitDto) : null;
    }

    private Organization toOrganization(UnitDto unitDto) {
        return new Organization.Builder()
                   .withId(getNvaApiId(unitDto.getId(), ORGANIZATION_PATH))
                   .withName(unitDto.getUnitName())
                   .withLabels(unitDto.getUnitName())
                   .withAcronym(unitDto.getAcronym())
                   .withNearestPartOf(this.apply(unitDto.getParentUnit()))
                   .withHasPart(unitsToOrganizations(unitDto.getSubUnits()))
                   .withPartOf(unitsToOrganizations(unitDto.getParentUnits()))
                   .build();
    }

    private Set<Organization> unitsToOrganizations(List<UnitDto> unitDto) {
        return unitDto.stream().map(this).collect(Collectors.toSet());
    }
}
