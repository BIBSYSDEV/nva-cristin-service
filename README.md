# nva-cristin-projects

Lambda for fetching project data from the [Cristin API](https://api.cristin.no/v2/doc/index.html)


###GET cristin-projects?{parameters}

| Query parameter | Description |
| ------ | ------ |
| title | Title of the project, or part of the title. Accepts letters, digits, dash and whitespace. (Mandatory) |
| language | Preferred language for titles. Accepts 'nb' or 'en'. (Optional) |


####Response

Returns a JSON array of up to 10 projects, or an empty JSON array if no projects are found.

Example response body:

```json
[
  {
    "cristinProjectId": "2040291",
    "mainLanguage": "en",
    "titles": [
      {
        "title": "Opportunities and Challenges for Integrating Sámi Reindeer Herding Traditional Environmental Knowledge in Environmental Governance",
        "language": "en"
      }
    ],
    "participants": [
      {
        "cristinPersonId": "327970",
        "fullName": "Turi, Ellen Inga"
      }
    ],
    "institutions": [
      {
        "cristinInstitutionId": "231",
        "name": "Sámi allaskuvla/Sámi University of Applied Sciences",
        "language": "nb"
      }
    ],
    "fundings": [
      {
        "fundingSourceCode": "NFR",
        "projectCode": "270819"
      }
    ]
  },
  {
    "cristinProjectId": "415549",
    "mainLanguage": "no",
    "titles": [
      {
        "title": "Opptak og omsetning av radioaktive stoffer og tungmetall i reinsdyr",
        "language": "no"
      },
      {
        "title": "Absorption and metabolism of radio active materials and heavy metals in reindeer",
        "language": "en"
      }
    ],
    "participants": [
      {
        "cristinPersonId": "135157",
        "fullName": "Skuterud, Lavrans"
      },
      {
        "cristinPersonId": "317528",
        "fullName": "Hove, Knut"
      },
      {
        "cristinPersonId": "317890",
        "fullName": "Strand, Per"
      }
    ],
    "institutions": [
      {
        "cristinInstitutionId": "192",
        "name": "Norges miljø- og biovitenskapelige universitet",
        "language": "nb"
      }
    ],
    "fundings": []
  }
]
```


####HTTP Status Codes

* 200 - Ok, returns an array of 0-10 projects.
* 400 - Bad request, returned if the parameters are invalid.
* 500 - Internal server error, returned if a problem is encountered retrieving project data