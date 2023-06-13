package no.unit.nva.cristin.organization.dto.v20230526.mapper;

import static java.util.Objects.nonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
        if (!hasParentUnit(unitDto)) {
            return;
        }

        var match = allParents
                        .stream()
                        .filter(hasId())
                        .filter(hasMatch(unitDto))
                        .findAny();

        if (match.isPresent()) {
            var presentMatch = match.get();
            unitDto.setParentUnit(presentMatch);
            allParents.remove(presentMatch);
            calculate(unitDto.getParentUnit());
        }
    }

    private boolean hasParentUnit(UnitDto unitDto) {
        return nonNull(unitDto.getParentUnit()) && nonNull(unitDto.getParentUnit().getId());
    }

    private Predicate<UnitDto> hasId() {
        return possibleParent -> nonNull(possibleParent.getId());
    }

    private Predicate<UnitDto> hasMatch(UnitDto unitDto) {
        return possibleParent -> possibleParent.getId().equals(unitDto.getParentUnit().getId());
    }

    public UnitDto getWithParentTrail() {
        return input;
    }

}
