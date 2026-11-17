# ADR: Clinical trial (KBS) reporting

- Feature description: `clinical-trial.md`

## Decisions

2. **ClinicalTrial is the whole trial; Submission is one period.** One ClinicalTrial per project, one
   Submission per project per period. "Period" and "report" are already taken (the shared window and
   the admin's output), so the per-period object is a submission.
5. **POST creates and PUT updates, instead of a single upsert.** POST needs no ETag and returns 409 if
   the resource exists; PUT always requires `If-Match` (428 if missing, 412 on mismatch). One method
   for both would either let a missing `If-Match` silently create or overwrite, or require an
   exception to the "every PUT requires `If-Match`" rule.
6. **One `save` per entity in the service and repository.** The handler maps the method to the
   revision: POST → `null` (condition `attribute_not_exists`), PUT → the `If-Match` revision
   (condition `revision = :expected`).