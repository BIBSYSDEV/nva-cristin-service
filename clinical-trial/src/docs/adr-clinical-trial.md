# ADR: Clinical trial (KBS) reporting

- Feature description: `clinical-trial.md`

## Decisions

1. **ClinicalTrial is the whole trial; Submission is one period.** One ClinicalTrial per project, one
   Submission per project per period. "Period" and "report" are already taken (the shared window and
   the admin's output), so the per-period object is a submission.
2. **POST creates and PUT updates, instead of a single upsert.** POST needs no ETag and returns 409 if
   the resource exists; PUT always requires `If-Match` (428 if missing, 412 on mismatch). One method
   for both would either let a missing `If-Match` silently create or overwrite, or require an
   exception to the "every PUT requires `If-Match`" rule.
3. **POST and PUT on the item path, although the service elsewhere creates with POST on a collection
   and updates with PATCH on an item.** The clinical trial and the submission are singletons whose
   identity is the project and the period in the path, so there is no collection to post to, and
   PATCH would need merge semantics for `inclusions`, which are always written in full.
4. **One `save` per entity in the service and repository.** The handler maps the method to the
   command: POST -> `create` (condition `attribute_not_exists`), PUT -> `update` with the `If-Match`
   version (condition `version = :expected`).
5. **`version` is both the optimistic lock and the ETag.** One counter on the item, raised by every
   write, served as `ETag` and required back in `If-Match`. A separate revision or timestamp token
   would be a second thing to keep in step with the condition expression.
6. **`status` is server-controlled.** The request carries inclusions only. A client that could send
   `REPORTED` could mark its own numbers delivered before the admin output ran.
7. **A write is a command carrying the caller, not the entity.**
   `save(SubmissionWrite)`: the expected version is a
   precondition, not state of the entity, and the caller has to reach the Version item for the audit
   to answer "who". `action` field - create and update are the two factories on the command, so
   the condition expression decides. `User` is a record of this module, built from `RequestInfo` in the handler;
