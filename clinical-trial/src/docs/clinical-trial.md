# Clinical trial (KBS)

Reporting of clinical treatment studies (kliniske behandlingsstudier, KBS): which projects qualify
as clinical trials, and how many participants each institution has included in each period.

## Terms

| Term          | Meaning                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------- |
| ClinicalTrial | The project as a clinical trial. One per project.                                                 |
| Submission    | Inclusions per institution for one period, with a status. One per project per period.             |
| Inclusion     | Number of participants included at one institution during one period.                             |
| Period        | Shared reporting window, such as `2026-H1`. Opened and closed by the admin side, not by this API. |
| Version       | Counter on a stored entity, raised by every write. Served as `ETag`, required back in `If-Match`. The entity as it was at that count is kept as a Version item. |

## Endpoints

| Method | Body                                                                     | Path                                                      | Handler                     | Service method                                                |
| ------ | ------------------------------------------------------------------------ | --------------------------------------------------------- | --------------------------- |---------------------------------------------------------------|
| GET    | [response](#clinical-trial-response)                                     | `/project/{identifier}/clinical-trial`                    | `FetchClinicalTrialHandler` | `ClinicalTrialService.find(projectIdentifier)`                |
| POST   | [request](#clinical-trial-request), [response](#clinical-trial-response) | `/project/{identifier}/clinical-trial`                    | `SaveClinicalTrialHandler`  | `ClinicalTrialService.save(clinicalTrialWrite)`               |
| PUT    | [request](#clinical-trial-request), [response](#clinical-trial-response) | `/project/{identifier}/clinical-trial`                    | `SaveClinicalTrialHandler`  | `ClinicalTrialService.save(clinicalTrialWrite)`               |
| GET    | [response](#submission-response)                                         | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `FetchSubmissionHandler`    | `SubmissionService.find(projectIdentifier, periodIdentifier)` |
| POST   | [request](#submission-request), [response](#submission-response)         | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `SaveSubmissionHandler`     | `SubmissionService.save(submissionWrite)`                     |
| PUT    | [request](#submission-request), [response](#submission-response)         | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `SaveSubmissionHandler`     | `SubmissionService.save(submissionWrite)`                     |

A write is one command: `ClinicalTrialWrite` / `SubmissionWrite`, built by the handler from the
entity, version , action and the caller.

POST creates: no `If-Match`, `409` if the resource exists. PUT updates: `If-Match` required, `428`
when it is missing, `412` when it does not match, `404` when the resource does not exist. Every
response carries the current version in `ETag`.

### Changes to existing endpoints

`GET /project/{identifier}` (`FetchCristinProjectHandler`) gains two fields, see
[GET project](#get-project). Both are derived from the project payload itself, so the project
lookup reads nothing from this feature's storage and `cristin-project` keeps no dependency on this
module.

## Bodies

### GET project

Existing project response, with two new fields:

```json
{
  "type": "Project",
  "id": "https://api.nva.unit.no/cristin/project/987654",
  "clinicalTrialEligible": true,
  "clinicalTrial": "https://api.nva.unit.no/cristin/project/987654/clinical-trial"
}
```

`clinicalTrial` is the link to the clinical trial of an eligible project, present whether or not
anything has been saved there yet. Both fields are omitted when the project is not eligible.

### Clinical trial request

```json
{ "reportingDecision": "Include" | "Exclude" }
```

### Clinical trial response

```json
{
  "type": "ClinicalTrial",
  "@context": "https://bibsysdev.github.io/src/project-context.json",
  "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial",
  "project": "https://api.nva.unit.no/cristin/project/987654",
  "reportingDecision": "Include",
  "submissions": [
    {
      "type": "Submission",
      "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2026-H1",
      "period": "https://api.nva.unit.no/cristin/clinical-trial/period/2026-H1",
      "status": "SUBMITTED"
    },
    {
      "type": "Submission",
      "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2025-H2",
      "period": "https://api.nva.unit.no/cristin/clinical-trial/period/2025-H2",
      "status": "REPORTED"
    }
  ]
}
```

Submissions are listed without their inclusions; a submission is read in full through its own GET.

### Submission request

```json
{
  "inclusions": [
    {
      "institution": "https://api.nva.unit.no/cristin/organization/185.0.0.0",
      "count": 9
    }
  ]
}
```

### Submission response

```json
{
  "type": "Submission",
  "@context": "https://bibsysdev.github.io/src/project-context.json",
  "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2026-H1",
  "project": "https://api.nva.unit.no/cristin/project/987654",
  "period": "https://api.nva.unit.no/cristin/clinical-trial/period/2026-H1",
  "status": "SUBMITTED",
  "inclusions": [
    {
      "type": "Inclusion",
      "institution": "https://api.nva.unit.no/cristin/organization/185.0.0.0",
      "count": 9
    }
  ]
}
```

## Storage

| Type          | PK               | SK                              | Attributes                          |
| ------------- | ---------------- | ------------------------------- | ----------------------------------- |
| ClinicalTrial | `PROJECT#987654` | `CLINICAL_TRIAL`                | `reportable`, `version`             |
| Submission    | `PROJECT#987654` | `SUBMISSION#2026-H1`            | `status`, `inclusions[]`, `version` |
| Period        | `PERIOD`         | `2026-H1`                       | `status`                            |
| Version       | entity's PK      | `VERSION#<entity SK>#<version>` | the entity item, `modifiedBy`, `modifiedByCustomer`, `modifiedAt` |

Every write raises `version` on the entity and writes one Version item in the same transaction.
Version items are the audit trail of who changed reported numbers; nothing in this API reads them,
and they are never pruned.

Listing every submission in a period, which the admin output needs, is not part of this feature. It
cannot be answered by this key layout and needs an index on `PERIOD#<period>` / `PROJECT#<identifier>`.

## Rules

- Eligible = the project has an approval with status `Approved` and `healthProjectData.type` is
  `Drugstudy` or `Otherclin`. Calculated on read, never stored. Only on the single project GET.
- Inclusions: each institution takes part in the project, at most once, `count` >= 0; all in one
  call, replacing the inclusions already saved for the period. An institution left out of the call
  has reported nothing, which is not the same as a `count` of 0. Persisted institution is
  identifier, not uri.
- A period is `OPEN` or `CLOSED`. A submission is written only while its period is `OPEN`, `409`
  otherwise. The clinical trial is not period-scoped and is written either way.
- A submission is `SUBMITTED` or `REPORTED`, set by the service, never by the request: `SUBMITTED`
  while its period is `OPEN`, `REPORTED` once the period is closed and delivered to the admin
  output.
- Writing a clinical trial or a submission requires the caller to be the project's manager: a
  `contributors[]` entry whose `roles[].type` is `ProjectManager` and whose `identity.id` is the
  caller's person. User with access right MANAGE_KBS is allowed to write ClinicalTrial and Submission as well. `UpdateProjectResourceAccessCheck` in `cristin-project` already makes that check
  for project update.
- Only Norwegian institutions report inclusions

## Open questions

1. `GET` of clinical trial in a period with no submission: `NOT_SUBMITTED` response?
2. How is "included nobody this period" recorded, so the admin output can tell "reported zero" from
   "not reported"? A `NO_INCLUSIONS` status on a submission saved with an empty `inclusions[]`, or
   something else?
3. May `reportable` change from `YES` to `NO` once a submission carries inclusions, or is that
   blocked until the inclusions are cleared?
4. The admin side closing a period writes `REPORTED` with no user behind it. Does the command take
   a system actor, or does that write go another way?
5. Does the approval have to come from a particular authority, or does any approval qualify?
6. The project context is served from `bibsysdev.github.io` today. Is the versioned
   `/cristin/project/context/{version}` plus `/cristin/project/ontology#` hosting landing before this
   feature, and should the examples use it instead?
