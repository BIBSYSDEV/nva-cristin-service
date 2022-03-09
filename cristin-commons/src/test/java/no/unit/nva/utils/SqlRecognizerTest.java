package no.unit.nva.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.model.CristinUnit;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlRecognizerTest {

    private static final String INSTITUTIONS_JSON = "institutions.json";

    @Test
    void legalQueryPassThru() {
        String query = "Ola+Nordmann";
        assertFalse(SqlRecognizer.containsSqlInjectionFragmentCandidate(query));
    }

    @Test
    void legalQueryWithDuplicateValues() {
        String query = "reindeer reindeer";
        assertFalse(SqlRecognizer.containsSqlInjectionFragmentCandidate(query));
    }

    @Test
    void recognizeSimpleAndQuery() {
        String query = "Ola+Nordmann and 1=1";
        assertTrue(SqlRecognizer.containsSqlInjectionFragmentCandidate(query));
    }

    @Test
    void recognizeStatementQuery() {
        String query = "Ola+Nordmann '';DROP DATABASE (DB Name) --' and password=''";
        assertTrue(SqlRecognizer.containsSqlInjectionFragmentCandidate(query));
    }

    @Test
    void checkNamesQuery() throws JsonProcessingException {
        CristinUnit[] units = OBJECT_MAPPER.readValue(IoUtils.stringFromResources(Path.of(INSTITUTIONS_JSON)), CristinUnit[].class);
        assertNotNull(units);
        List<String> names = new ArrayList<>();
        for (CristinUnit unit : units) {
            names.addAll(unit.getUnitName().entrySet()
                    .stream()
                    .map(Map.Entry::getValue)
                    .filter(SqlRecognizer::containsSuspicousInjectionFragmentCandidate)
                    .collect(Collectors.toList()));
            names.stream().filter(Objects::nonNull).forEach(System.out::println);
        }
    }

    @Test
    void checkStatements() {
        List<String> candidates = List.of("Kémerovskij gosudárstvennyj universitét");
        List<String> names = new ArrayList<>();
        names.addAll(candidates.stream()
                .filter(SqlRecognizer::containsSuspicousInjectionFragmentCandidate)
                .collect(Collectors.toList()));

        names.stream().filter(Objects::nonNull).forEach(System.out::println);
    }
}



//    @Test
//    void checkNames2Query() throws JsonProcessingException {
//        JsonNode units = OBJECT_MAPPER.readTree(IoUtils.stringFromResources(Path.of(INSTITUTIONS_JSON)));
//        units.findValuesAsText("unit_name").stream().forEach(System.out::println);
//
//        assertNotNull(units);
//    }





