# Clinical trial (KBS)

Projects that qualify as clinical trials (KBS), per period, how many
participants each institution has included.

## Terms

| Term | Meaning                                                                                |
|---|----------------------------------------------------------------------------------------|
| ClinicalTrial | The project as a clinical trial. One per project.                                      |
| Submission | Inclusions per institution for one period, with a status. One per project per period.  |
| Inclusion | Number of participants included at one institution for clinical trial during a period. |
| Period | Shared reporting window.                                                               |

## Endpoints

| Method | Body | Path | Handler                      | Service method |
|--------|---|---|------------------------------|---|
| GET    | [response](#get-project) | `/project/{identifier}` | `FetchCristinProjectHandler` | `EligibilityService.isEligible(project)` |
| GET    | [response](#clinical-trial-response) | `/project/{identifier}/clinical-trial` | `FetchClinicalTrialHandler`  | `ClinicalTrialService.fetch(projectIdentifier)` |
| PUT    | [request](#clinical-trial-request), [response](#clinical-trial-response) | `/project/{identifier}/clinical-trial` | `UpsertClinicalTrialHandler` | `ClinicalTrialService.save(clinicalTrial)` |
| POST   | [request](#clinical-trial-request), [response](#clinical-trial-response) | `/project/{identifier}/clinical-trial` | `UpsertClinicalTrialHandler` | `ClinicalTrialService.save(clinicalTrial)` |
| GET    | [response](#submission-response) | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `FetchSubmissionHandler`     | `SubmissionService.fetch(projectIdentifier, periodIdentifier)` |
| PUT    | [request](#submission-request), [response](#submission-response) | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `UpsertSubmissionHandler`    | `SubmissionService.save(submission)` |
| POST   | [request](#submission-request), [response](#submission-response) | `/project/{identifier}/clinical-trial/{periodIdentifier}` | `UpsertSubmissionHandler`    | `SubmissionService.save(submission)` |

Context should be served with each GET
- `/cristin/project/context/{version}`
- `/cristin/project/ontology#`

## Bodies

### GET project

Existing project response, with two new fields:

```json
{
  "type": "Project",
  "id": "https://api.nva.unit.no/cristin/project/987654",
  "reportable": true,
  "clinicalTrial": "https://api.nva.unit.no/cristin/project/987654/clinical-trial"
}
```

### Clinical trial request

```json
{ "reportable": "YES" }
```

### Clinical trial response

```json
{
  "type": "ClinicalTrial",
  "@context": "https://api.nva.unit.no/cristin/project/context",
  "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial",
  "project": "https://api.nva.unit.no/cristin/project/987654",
  "reportable": "YES",
  "submissions": [
    {
      "type": "Submission",
      "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2026-H1",
      "period": "https://api.nva.unit.no/clinical-trial/period/2026-H1",
      "status": "SUBMITTED"
    },
    {
      "type": "Submission",
      "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2025-H2",
      "period": "https://api.nva.unit.no/clinical-trial/period/2025-H2",
      "status": "REPORTED"
    }
  ]
}
```

### Submission request

```json
{
  "inclusions": [
    { "institution": "https://api.nva.unit.no/cristin/organization/185.0.0.0", "count": 9 }
  ]
}
```

### Submission response

```json
{
  "type": "Submission",
  "@context": "https://api.nva.unit.no/cristin/project/context",
  "id": "https://api.nva.unit.no/cristin/project/987654/clinical-trial/2026-H1",
  "project": "https://api.nva.unit.no/cristin/project/987654",
  "period": "https://api.nva.unit.no/clinical-trial/period/2026-H1",
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

| Type | PK | SK                              | Attributes                     |
|---|---|---------------------------------|--------------------------------|
| ClinicalTrial | `PROJECT#987654` | `CLINICAL_TRIAL`                | `version` |
| Submission | `PROJECT#987654` | `SUBMISSION#2026-H1`            | `status`, `inclusions[]`, `version` |
| Period | `PERIOD` | `2026-H1`                       | `status`, `version`            |
| History | entity's PK | `VERSION#<entity SK>#<version>` | EntityDao                      |

Every write also writes one Version item in the same transaction.

## Rules

- Eligible = the project has an approval and its category is `Drugstudy` or `Otherclin`. Calculated on read, never stored.
- Inclusions: each institution takes part in the project, at most once, `count` ≥ 0; all in one call. Persisted institution is identifier, not uri.
- Versions and ETags: every GET returns an `ETag` built from the entity's `version`. Every PUT
  requires `If-Match` with that ETag (428 if missing) and writes conditionally on
  `version = :expected` (412 if it does not match), incrementing `version`. POST writes
  conditionally on `attribute_not_exists` (409 if it exists) and returns the new `ETag`.
