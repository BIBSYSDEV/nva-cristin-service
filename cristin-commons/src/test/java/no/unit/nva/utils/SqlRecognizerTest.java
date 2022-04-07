package no.unit.nva.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlRecognizerTest {

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
    void checkStatements() {
        List<String> candidates = List.of("Kémerovskij gosudárstvennyj universitét");
        List<String> names = new ArrayList<>();
        names.addAll(candidates.stream()
                .filter(SqlRecognizer::containsSqlInjectionFragmentCandidate)
                .collect(Collectors.toList()));
        names.stream().filter(Objects::nonNull).forEach(System.out::println);
    }
}





