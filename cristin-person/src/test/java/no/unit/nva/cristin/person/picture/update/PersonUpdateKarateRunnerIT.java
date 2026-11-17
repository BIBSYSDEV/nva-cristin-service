package no.unit.nva.cristin.person.picture.update;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class PersonUpdateKarateRunnerIT {

  @Karate.Test
  Iterable<DynamicNode> runKarateFeatures() {
    return Karate.run().relativeTo(getClass());
  }
}
