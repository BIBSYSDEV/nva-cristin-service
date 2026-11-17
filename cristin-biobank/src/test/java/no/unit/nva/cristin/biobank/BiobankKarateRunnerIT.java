package no.unit.nva.cristin.biobank;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class BiobankKarateRunnerIT {

  @Karate.Test
  Iterable<DynamicNode> runBiobankKarateTests() {
    return Karate.run().relativeTo(getClass());
  }
}
