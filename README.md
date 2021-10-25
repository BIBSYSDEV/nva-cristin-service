# nva-cristin-service

Lambda for fetching project data from the [Cristin API](https://api.cristin.no/v2/doc/index.html)


### GET cristin-projects?{parameters}

| Query parameter | Description |
| ------ | ------ |
| query | Either title of the project, part of the title, or a grant id. Accepts letters, digits, dash and whitespace. (Mandatory) |
| language | Preferred language for titles. Accepts 'nb', 'nn' or 'en'. (Optional) |
| page | Pagination for current page requested. |
| results | Results per page. |


#### Response

Returns an Json object with projects matching search query
and associated metadata of the search. 
Projects are in field 'hits' which is an array. 
If no projects are found, 'hits' returns empty array.

Example response body:

```json
{
  "@context": "https://example.org/search-api-context.json",
  "id": "https://api.dev.nva.aws.unit.no/project/?language=nb&page=1&query=reindeer&results=5",
  "size": 135,
  "searchString": "language=nb&page=1&query=reindeer&results=5",
  "processingTime": 1000,
  "firstRecord": 1,
  "nextResults": "https://api.dev.nva.aws.unit.no/project/?language=nb&page=2&query=reindeer&results=5",
  "previousResults": null,
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
      "language": "http://lexvo.org/id/iso639-3/nno",
      "startDate": "2018-11-01T00:00:00Z",
      "endDate": "2021-05-01T00:00:00Z",
      "funding": [
        {
          "type": "Funding",
          "source": {
            "type": "FundingSource",
            "names": {
              "nb": "Egen institusjon"
            },
            "code": "EI"
          }
        }
      ],      
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
      ],
      "academicSummary": {
        "en": "an academic summary og the project, in english"
      },
      "popularScientificSummary": {}
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
      "language": "http://lexvo.org/id/iso639-3/nno",
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
      ],
      "status": "CONCLUDED",
      "academicSummary": {
        "no": "Jeg arbeider med å skrive en bok, med foreløpig tittel, Digitale bilder. Jeg har undervist i studieemnet digitale bilder i flere år, og det har vært et problem å skaffe egnet litteratur. Boken skal være en arbeids- og øvingsbok for nybegynnere i bruk av Photoshop. Målgruppen er studenter på høgskolen eller andre som skal arbeide med webdesign."
      },
      "popularScientificSummary": {
        "no": "<p>FoU-prosjektet skal fremme forskningsbasert undervisning i digitale medier knyttet til Faglærerutdanningen i formgiving, kunst og håndverk på Institutt for estetiske fag. Undersøkelsen skal vise hvordan studentene uttrykker seg i digital bildeskaping? Hvordan de bruker Photoshop for å lage bilder?</p>\r\n"
      }
    }
  ]
}
```


#### HTTP Status Codes

*   200 - Ok, returns an array of 0-5 projects.
*   400 - Bad request, returned if the parameters are invalid.
*   500 - Internal server error, returned if a problem is encountered retrieving project data

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
  "language": "http://lexvo.org/id/iso639-3/nno",
  "startDate": "2018-11-01T00:00:00Z",
  "endDate": "2021-05-01T00:00:00Z",
  "funding": [
    {
      "type": "Funding",
      "source": {
        "type": "FundingSource",
        "names": {
          "nb": "Egen institusjon"
        },
        "code": "EI"
      }
    }
  ],  
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
  ],
  "status": "CONCLUDED",
  "academicSummary": {
    "no": "<p>\r\n\tHelsedirektoratet har gjennom &rdquo;Underern&aelig;ringsprosjektet&rdquo; satt fokus p&aring; ern&aelig;ringssituasjonen p&aring; sykehus og institusjoner. Som et ledd i dette arbeidet skal gjeldende forskningsprosjekt unders&oslash;ke matservering/pasientbespisning ved sykehuset Ahus. M&aring;let for prosjektet er &aring; unders&oslash;ke sammenhengen mellom organiseringen av bespisningen og trivsel, matopptak og ern&aelig;ringssituasjon for pasientene. Fokus vil v&aelig;re p&aring; selve tjenesteleveransen og mindre p&aring; den ern&aelig;ringsmessige sammensetningen av mattilbudet. Bakgrunnen er at matserveringen p&aring; Ahus er et omr&aring;de som oppleves som problematisk av ansatte p&aring; flere av sykehusets mange avdelinger samt av ulike faggrupper og pasienter. Prosjektet gjennomf&oslash;res i samarbeid med Sykehuset Ahus og er tenkt knyttet til Helsedirektoratets satsing p&aring; forebyggende behandling av underern&aelig;ring.</p>\r\n"
  },
  "popularScientificSummary": {}  
}
```

#### HTTP Status Codes lookup

*   200 - Ok, returns one project.
*   400 - Bad request, returned if the parameters are invalid.
*   500 - Internal server error, returned if a problem is encountered retrieving project data
*   502 - Bad Gateway, returned if upstream fetch fails or project not found
