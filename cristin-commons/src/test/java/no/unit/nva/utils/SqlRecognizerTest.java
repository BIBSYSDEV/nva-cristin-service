package no.unit.nva.utils;

import org.junit.jupiter.api.Test;

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
}



