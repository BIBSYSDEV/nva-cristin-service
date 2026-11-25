package no.unit.nva.clinicaltrial.persistence.v2;

import java.time.Instant;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Writer;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;

/** One item of a versioned write: the row to write, and the table it belongs to. */
record VersionedItem<E extends Entity, D extends VersionedDao<E>>(DynamoDbTable<D> table, D dao) {

  void addTo(TransactWriteItemsEnhancedRequest.Builder transaction) {
    transaction.addUpdateItem(table, updateRequest());
  }

  VersionDao versionDao(Writer writer, Instant timestamp) {
    var entity = dao.toEntity();
    return VersionDao.versionOf(
        dao, SortableIdentifier.next(), entity, writer, entity.action(), timestamp);
  }

  private TransactUpdateItemEnhancedRequest<D> updateRequest() {
    return TransactUpdateItemEnhancedRequest.builder(table.tableSchema().itemType().rawClass())
        .item(dao)
        .ignoreNulls(true)
        .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
        .build();
  }
}
