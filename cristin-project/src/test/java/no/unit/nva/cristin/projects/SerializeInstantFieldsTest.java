package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomMinimalNvaProject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.projects.model.cristin.CristinProjectBuilder;
import org.junit.jupiter.api.Test;

class SerializeInstantFieldsTest {

    public static final ObjectMapper OBJECT_MAPPER_NON_EMPTY = JsonUtils.dynamoObjectMapper;

    @Test
    void serializeRandomProjectWithLossLessInstantTest() throws JsonProcessingException {
        var sampleInstantStringWithZeroMillis = "2021-03-01T12:00:00.000Z";
        var instant = Instant.parse(sampleInstantStringWithZeroMillis);
        var nvaProject = randomMinimalNvaProject();
        nvaProject.setStartDate(instant);
        var cristinProject = new CristinProjectBuilder().apply(nvaProject);
        var projectAsJson = OBJECT_MAPPER_NON_EMPTY.writeValueAsString(cristinProject);
        var tree = OBJECT_MAPPER_NON_EMPTY.readTree(projectAsJson);
        var convertedInstant = tree.get("start_date").asText();

        assertEquals(sampleInstantStringWithZeroMillis, convertedInstant);
    }

}

