Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def FEIDE_ID = 'karate-user-administrator@sikt.no'
    * def PASSWORD = 'p@ssW0rd'
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = new tokenGenerator().loginUser(FEIDE_ID, PASSWORD)
    * def illegalIdentifier = 'illegalIdentifier'
    * def samplePersonId = '07117631634'
    * def nonExistingPersonId = '11077941012'
    * def personIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/person\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    * def invalidToken = 'just-a-invalid-token-for-now'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario Outline: Fetch returns valid data and with correct content negotiation <CONTENT_TYPE>
    * configure headers = { 'Accept': <CONTENT_TYPE> }
    Given path '/person/identityNumber'
    * header Authorization = 'Bearer ' + token
    And request { type : NationalIdentificationNumber, value : '07117631634' }
    When method POST
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'

    Examples:
      | CONTENT_TYPE       |
      # TODO: Implement ld json
      #| 'application/ld+json' |
      | 'application/json' |

  Scenario: Fetch returns status 401 Forbidden when requesting NationalIdentificationNumber without valid token
    Given path '/person/identityNumber'
    * header Authorization = 'Bearer ' + invalidToken
    And request { type : NationalIdentificationNumber, value : '07117631634' }
    When method POST
    Then status 401

  Scenario Outline: Query with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    * header Authorization = 'Bearer ' + token
    Given path '/person/identityNumber'
    And request { type : NationalIdentificationNumber, value : '07117631634' }
    When method POST
    Then status 415
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Unsupported Media Type'
    And match response.status == 415
    And match response.detail == <UNACCEPTABLE_CONTENT_TYPE> + ' contains no supported Accept header values. Supported values are: application/json; charset=utf-8'
    And match response.requestId == '#notnull'

    Examples:
      | UNACCEPTABLE_CONTENT_TYPE |
      | 'image/jpeg'              |
      | 'application/xml'         |
      | 'application/rdf+xml'     |

  Scenario: Fetch returns status Bad request when requesting illegal person identifier
    Given path '/person/identityNumber'
    * header Authorization = 'Bearer ' + token
    And request { type : NationalIdentificationNumber, value : 'abcdefghij' }
    When method POST
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Supplied payload is not valid'


  Scenario: Fetch returns status Bad request when sending illegal query parameter
    Given path '/person/identityNumber'
    * header Authorization = 'Bearer ' + token
    And request { type : SwedishIdentificationNumber, value : 'medelsvenson' }
    When method POST
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Supplied payload is not valid'

  Scenario: Fetch returns status Not found when requesting unknown person identifier
    Given path '/person/identityNumber'
    * header Authorization = 'Bearer ' + token
    And request { type : NationalIdentificationNumber, value : '11077941012' }
    When method POST
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == "No match found for supplied payload"
