package no.unit.nva.clinicaltrial.persistence.dao;

import java.time.Instant;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Submission;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

public interface VersionedDao<E extends Entity> extends Dao {

  String MISSING_VERSION = "Row does not carry a version";

  static VersionedDao<?> fromEntity(Entity entity, long version, Instant modifiedDate) {
    return switch (entity) {
      case ClinicalTrial trial -> ClinicalTrialDao.from(trial, version, modifiedDate);
      case Period period -> PeriodDao.from(period, version, modifiedDate);
      case Submission submission -> SubmissionDao.from(submission, version, modifiedDate);
    };
  }

  static <E extends Entity, D extends VersionedDao<E>> E entityOf(
      EnhancedDocument document, Class<D> daoType) {
    var dao = Dao.fromDocument(document, daoType);
    if (Objects.isNull(dao.version())) {
      throw new IllegalStateException(MISSING_VERSION);
    }
    return dao.toEntity();
  }

  Long version();

  E toEntity();
}
