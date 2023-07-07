package no.unit.nva.cristin.organization.dto.v20230526.mapper;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;
import no.unit.nva.model.Organization;

public class OrganizationFromUnitMapper implements Function<UnitDto, Organization> {

    @Override
    public Organization apply(UnitDto unitDto) {
        return Optional.ofNullable(unitDto)
                   .map(ParentTrailMapper::new)
                   .map(ParentTrailMapper::getWithParentTrail)
                   .map(SubTreeMapper::new)
                   .map(SubTreeMapper::getWithSubUnitTree)
                   .map(this::toOrganization)
                   .orElse(null);
    }

    private Organization toOrganization(UnitDto unitDto) {
        return new Organization.Builder()
                   .withId(getNvaApiId(unitDto.getId(), ORGANIZATION_PATH))
                   .withLabels(unitDto.getUnitName())
                   .withAcronym(unitDto.getAcronym())
                   .withHasPart(unitsToOrganizations(unitDto.getSubUnits()))
                   .withPartOf(parentToSetOfOrganizations(unitDto.getParentUnit()))
                   .build();
    }

    private Set<Organization> unitsToOrganizations(List<UnitDto> unitDtos) {
        return unitDtos.stream().map(this::toOrganization).collect(Collectors.toSet());
    }

    private Set<Organization> parentToSetOfOrganizations(UnitDto parent) {
        return Optional.ofNullable(parent)
                   .map(this)
                   .stream()
                   .collect(Collectors.toSet());
    }

}
