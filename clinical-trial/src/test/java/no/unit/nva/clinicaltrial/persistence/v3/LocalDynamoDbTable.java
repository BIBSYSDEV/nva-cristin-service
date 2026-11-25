package no.unit.nva.clinicaltrial.persistence.v3;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.dynamodb.services.local.embedded.DynamoDBEmbedded;

public final class LocalDynamoDbTable {

  public static ClinicalTrialRepository repositoryOf(DynamoDbClient client, String tableName) {
    return new ClinicalTrialRepository(
        new VersionedTable(
            DynamoDbEnhancedClient.builder().dynamoDbClient(client).build(), tableName));
  }

  private static final String PARTITION_KEY = "PK";
  private static final String SORT_KEY = "SK";
  private static final Long CAPACITY_DOES_NOT_MATTER = 1000L;

  private LocalDynamoDbTable() {}

  public static DynamoDbClient create(String tableName) {
    var client = DynamoDBEmbedded.create(null, true).dynamoDbClient();
    client.createTable(createTableRequest(tableName));
    return client;
  }

  private static CreateTableRequest createTableRequest(String tableName) {
    return CreateTableRequest.builder()
        .tableName(tableName)
        .attributeDefinitions(stringAttribute(PARTITION_KEY), stringAttribute(SORT_KEY))
        .keySchema(keyElement(PARTITION_KEY, KeyType.HASH), keyElement(SORT_KEY, KeyType.RANGE))
        .provisionedThroughput(
            ProvisionedThroughput.builder()
                .readCapacityUnits(CAPACITY_DOES_NOT_MATTER)
                .writeCapacityUnits(CAPACITY_DOES_NOT_MATTER)
                .build())
        .build();
  }

  private static AttributeDefinition stringAttribute(String name) {
    return AttributeDefinition.builder()
        .attributeName(name)
        .attributeType(ScalarAttributeType.S)
        .build();
  }

  private static KeySchemaElement keyElement(String name, KeyType type) {
    return KeySchemaElement.builder().attributeName(name).keyType(type).build();
  }
}
