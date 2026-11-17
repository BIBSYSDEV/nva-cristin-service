package no.unit.nva.cristin.funding.sources;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class FundingSourcesKarateRunnerIT {

  @Karate.Test
  Iterable<DynamicNode> runFundingSourcesKarateTests() {
    return Karate.run().relativeTo(getClass());
  }
}
