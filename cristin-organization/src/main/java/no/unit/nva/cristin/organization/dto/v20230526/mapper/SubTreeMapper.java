package no.unit.nva.cristin.organization.dto.v20230526.mapper;

import java.util.ArrayList;
import java.util.List;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;

public class SubTreeMapper {

    private final transient UnitDto input;
    private final transient List<UnitDto> allSubUnits;

    /**
     * Enrichment class which enriches given input with the subunit path tree structure derived from the parameter's
     * list of subunits.
     *
     * @param input The input model
     **/
    public SubTreeMapper(UnitDto input) {
        this.input = input;
        allSubUnits = new ArrayList<>(input.getSubUnits());
        if (allSubUnits.isEmpty()) {
            return;
        }
        calculate(input);
    }

    private void calculate(UnitDto unitDto) {
        var list = new ArrayList<UnitDto>();
        allSubUnits.forEach(subUnit -> {
            if (subUnit.getParentUnit() != null // TODO: Better null checks
                && subUnit.getParentUnit().getId() != null
                && subUnit.getParentUnit().getId().equals(unitDto.getId())) {

                list.add(subUnit);
            }
        });
        unitDto.setSubUnits(list);
        list.forEach(this::calculate);
    }

    public UnitDto getWithSubUnitTree() {
        return input;
    }

}
