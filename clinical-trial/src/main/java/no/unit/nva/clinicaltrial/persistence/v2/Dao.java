package no.unit.nva.clinicaltrial.persistence.v2;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Dao {

  private String partitionKey;
  private String sortKey;
  private Instant createdDate;
  private Instant modifiedDate;

  /** An item written once carries the same created and modified date. */
  public void timestamp(Instant timestamp) {
    setCreatedDate(timestamp);
    setModifiedDate(timestamp);
  }

  @DynamoDbPartitionKey
  @DynamoDbAttribute("PK")
  public String getPartitionKey() {
    return partitionKey;
  }

  public void setPartitionKey(String partitionKey) {
    this.partitionKey = partitionKey;
  }

  @DynamoDbSortKey
  @DynamoDbAttribute("SK")
  public String getSortKey() {
    return sortKey;
  }

  public void setSortKey(String sortKey) {
    this.sortKey = sortKey;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Instant createdDate) {
    this.createdDate = createdDate;
  }

  public Instant getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Instant modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
}
