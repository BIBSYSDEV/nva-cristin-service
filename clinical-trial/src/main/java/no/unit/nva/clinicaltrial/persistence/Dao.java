package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

public interface Dao {

  String partitionKey();

  String sortKey();

  Instant createdDate();

  Instant modifiedDate();

  TransactWriteItem toTransactWriteItem(String tableName);
}
