# nva-cristin-projects

Lambda for fetching project data from the [Cristin API](https://api.cristin.no/v2/doc/index.html)


### GET cristin-projects?{parameters}

| Query parameter | Description |
| ------ | ------ |
| title | Title of the project, or part of the title. Accepts letters, digits, dash and whitespace. (Mandatory) |
| language | Preferred language for titles. Accepts 'nb' or 'en'. (Optional) |


#### Response

Returns a JSON array of up to 10 projects, or an empty JSON array if no projects are found.

Example response body:

```json
[
  {
    "cristinProjectId": "456789",
    "mainLanguage": "en",
    "titles": [
      {
        "title": "Some Example Title",
        "language": "en"
      }
    ],
    "participants": [
      {
        "cristinPersonId": "123456",
        "fullName": "Nordmann, Ola"
      }
    ],
    "institutions": [
      {
        "cristinInstitutionId": "1234",
        "name": "Et universitet i Norge",
        "language": "nb"
      }
    ],
    "fundings": [
      {
        "fundingSourceCode": "NFR",
        "projectCode": "654321"
      }
    ]
  },
  {
    "cristinProjectId": "345678",
    "mainLanguage": "no",
    "titles": [
      {
        "title": "En eller annen tittel",
        "language": "no"
      },
      {
        "title": "Some example title",
        "language": "en"
      }
    ],
    "participants": [
      {
        "cristinPersonId": "223344",
        "fullName": "Nordmann, Kari"
      },
      {
        "cristinPersonId": "443322",
        "fullName": "Hansen, Knut"
      },
      {
        "cristinPersonId": "111222",
        "fullName": "Jensen, Per"
      }
    ],
    "institutions": [
      {
        "cristinInstitutionId": "9876",
        "name": "Et annet universitet",
        "language": "nb"
      }
    ],
    "fundings": []
  }
]
```


#### HTTP Status Codes

* 200 - Ok, returns an array of 0-10 projects.
* 400 - Bad request, returned if the parameters are invalid.
* 500 - Internal server error, returned if a problem is encountered retrieving project data