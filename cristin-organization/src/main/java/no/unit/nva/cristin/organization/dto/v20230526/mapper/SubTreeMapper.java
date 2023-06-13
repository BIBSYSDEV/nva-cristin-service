package no.unit.nva.cristin.organization.dto.v20230526.mapper;

import static java.util.Objects.nonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
        var results = allSubUnits.stream()
                          .filter(hasParentUnit())
                          .filter(hasMatch(unitDto))
                          .collect(Collectors.toList());

        unitDto.setSubUnits(results);
        allSubUnits.removeAll(results);
        results.forEach(this::calculate);
    }

    private Predicate<UnitDto> hasParentUnit() {
        return subUnit -> nonNull(subUnit.getParentUnit()) && nonNull(subUnit.getParentUnit().getId());
    }

    private Predicate<UnitDto> hasMatch(UnitDto unitDto) {
        return subUnit -> subUnit.getParentUnit().getId().equals(unitDto.getId());
    }

    public UnitDto getWithSubUnitTree() {
        return input;
    }

}
