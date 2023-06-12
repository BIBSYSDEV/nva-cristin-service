package no.unit.nva.cristin.organization.dto.v20230526.mapper;

import static java.util.Objects.nonNull;
import java.util.ArrayList;
import java.util.List;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;

public class ParentTrailMapper {

    private final transient UnitDto input;
    private final transient List<UnitDto> allParents;

    /**
     * Enrichment class which enriches given input with the parent path structure derived from the parameter's list of
     * parents.
     *
     * @param input The input model
    **/
    public ParentTrailMapper(UnitDto input) {
        this.input = input;
        allParents = new ArrayList<>(input.getParentUnits());
        if (allParents.isEmpty()) {
            return;
        }
        calculate(input);
    }

    private void calculate(UnitDto unitDto) {
        var match = allParents
                        .stream()
                        .filter(possibleParent -> nonNull(possibleParent.getId()))
                        // TODO: Make null safe
                        .filter(possibleParent -> possibleParent.getId().equals(unitDto.getParentUnit().getId()))
                        .findAny();

        if (match.isPresent()) {
            var presentMatch = match.get();
            unitDto.setParentUnit(presentMatch);
            allParents.remove(presentMatch);
            calculate(unitDto.getParentUnit());
        }
    }

    public UnitDto getWithParentTrail() {
        return input;
    }

}
