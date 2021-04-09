# nva-cristin-projects

Lambda for fetching project data from the [Cristin API](https://api.cristin.no/v2/doc/index.html)


### GET cristin-projects?{parameters}

| Query parameter | Description |
| ------ | ------ |
| title | Title of the project, or part of the title. Accepts letters, digits, dash and whitespace. (Mandatory) |
| language | Preferred language for titles. Accepts 'nb' or 'en'. (Optional) |


#### Response

Returns an Json object with projects matching search query
and associated metadata of the search. 
Projects are in field 'hits' which is an array. 
If no projects are found, 'hits' returns empty array.

Example response body:

```json
{
  "@context": "https://example.org/search-api-context.json",
  "id": "https://api.dev.nva.aws.unit.no/project/search?QUERY_PARAMS",
  "size": 0,
  "searchString": "title=reindeer",
  "processingTime": 1000,
  "firstRecord": 0,
  "nextResults": "",
  "hits": [
    {
      "id": "https://api.dev.nva.aws.unit.no/project/456789",
      "type": "Project",
      "identifier": [
        {
          "type": "CristinIdentifier",
          "value": "456789"
        }
      ],
      "title": "Example Title",
      "language": "https://lexvo.org/id/iso639-3/nno",
      "startDate": "2018-11-01T00:00:00Z",
      "endDate": "2021-05-01T00:00:00Z",
      "coordinatingInstitution": {
        "id": "https://api.cristin.no/v2/institutions/1234",
        "type": "Organization",
        "name": {
          "en": "University"
        }
      },
      "contributors": [
        {
          "type": "ProjectManager",
          "identity": {
            "id": "https://api.cristin.no/v2/persons/123456",
            "type": "Person",
            "firstName": "Ola",
            "lastName": "Nordmann"
          },
          "affiliation": {
            "id": "https://api.cristin.no/v2/institutions/1234",
            "type": "Organization",
            "name": {
              "en": "University"
            }
          }
        }
      ]
    },
    {
      "id": "https://api.dev.nva.aws.unit.no/project/456789",
      "type": "Project",
      "identifier": [
        {
          "type": "CristinIdentifier",
          "value": "456789"
        }
      ],
      "title": "Example Title",
      "language": "https://lexvo.org/id/iso639-3/nno",
      "startDate": "2018-11-01T00:00:00Z",
      "endDate": "2021-05-01T00:00:00Z",
      "coordinatingInstitution": {
        "id": "https://api.cristin.no/v2/institutions/1234",
        "type": "Organization",
        "name": {
          "en": "University"
        }
      },
      "contributors": [
        {
          "type": "ProjectManager",
          "identity": {
            "id": "https://api.cristin.no/v2/persons/123456",
            "type": "Person",
            "firstName": "Ola",
            "lastName": "Nordmann"
          },
          "affiliation": {
            "id": "https://api.cristin.no/v2/institutions/1234",
            "type": "Organization",
            "name": {
              "en": "University"
            }
          }
        }
      ]
    }
  ]
}
```


#### HTTP Status Codes

*  200 - Ok, returns an array of 0-5 projects.
*  400 - Bad request, returned if the parameters are invalid.
*  500 - Internal server error, returned if a problem is encountered retrieving project data


### GET cristin-projects/{id}?{language_parameter}
| parameter | description |
| ------ | ------ |
| id | The unique identifier of one project (Mandatory)
| language | Preferred language for titles. Accepts 'nb' or 'en'. (Optional) |


#### Lookup response

Returns an Json object containing one project

Example response body:

```json
{
  "@context": "https://example.org/project-context.json",
  "id": "https://api.dev.nva.aws.unit.no/project/456789",
  "type": "Project",
  "identifier": [
    {
      "type": "CristinIdentifier",
      "value": "456789"
    }
  ],
  "title": "Example Title",
  "language": "https://lexvo.org/id/iso639-3/nno",
  "startDate": "2018-11-01T00:00:00Z",
  "endDate": "2021-05-01T00:00:00Z",
  "coordinatingInstitution": {
    "id": "https://api.cristin.no/v2/institutions/1234",
    "type": "Organization",
    "name": {
      "en": "University"
    }
  },
  "contributors": [
    {
      "type": "ProjectManager",
      "identity": {
        "id": "https://api.cristin.no/v2/persons/123456",
        "type": "Person",
        "firstName": "Ola",
        "lastName": "Nordmann"
      },
      "affiliation": {
        "id": "https://api.cristin.no/v2/institutions/1234",
        "type": "Organization",
        "name": {
          "en": "University"
        }
      }
    }
  ]
}
```


#### HTTP Status Codes lookup

*  200 - Ok, returns one project.
*  400 - Bad request, returned if the parameters are invalid.
*  500 - Internal server error, returned if a problem is encountered retrieving project data
*  502 - Bad Gateway, returned if upstream fetch fails or project not found 
