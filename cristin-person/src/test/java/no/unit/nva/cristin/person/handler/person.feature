Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingPersonId = '999999'
    * def validIdentifier = '738'
    * def validOrcid = '0000-0002-3121-1236'
    * def nonExistingOrcid = '1111-1111-1111-1111'
    * def personIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/person\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch and receive CORS preflight response
    * configure headers =
  """
    {
      'Origin': 'http://localhost:3000',
      'Accept': '*/*',
      'Referer': 'Not sure what the value should be yet',
      'Connection': 'keep-alive',
      'Accept-Encoding': 'gzip, deflate, br',
      'Access-Control-Request-Method': 'GET'
    }
  """
    Given path '/person/1234'
    When method OPTIONS
    Then status 200
    And match responseHeaders['Access-Control-Allow-Origin'][0] == '*'
    * def accessControlAllowMethods = responseHeaders['Access-Control-Allow-Methods'][0]
    And match accessControlAllowMethods contains 'GET'
    And match accessControlAllowMethods contains 'OPTIONS'
    * def accessControlAllowHeaders = responseHeaders['Access-Control-Allow-Headers'][0]
    And match accessControlAllowHeaders contains 'Content-Type'
    And match accessControlAllowHeaders contains 'X-Amz-Date'
    And match accessControlAllowHeaders contains 'Authorization'
    And match accessControlAllowHeaders contains 'X-Api-Key'
    And match accessControlAllowHeaders contains 'X-Amz-Security-Token'
    And match accessControlAllowHeaders contains 'Access-Control-Allow-Origin'

  Scenario Outline: Fetch returns valid data and with correct content negotiation <CONTENT_TYPE>
    * configure headers = { 'Accept': <CONTENT_TYPE> }
    Given path '/person/' + validIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'

    Examples:
      | CONTENT_TYPE       |
      #| 'application/ld+json' | Must implement later
      | 'application/json' |

  Scenario Outline: Fetch with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/person/' + validIdentifier
    When method GET
    Then status 415
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Unsupported Media Type'
    And match response.status == 415
    #And match response.detail == <UNACCEPTABLE_CONTENT_TYPE> + ' contains no supported Accept header values. Supported values are: application/json; charset=utf-8, application/ld+json'
    And match response.detail == <UNACCEPTABLE_CONTENT_TYPE> + ' contains no supported Accept header values. Supported values are: application/json; charset=utf-8'
    And match response.requestId == '#notnull'

    Examples:
      | UNACCEPTABLE_CONTENT_TYPE |
      | 'image/jpeg'              |
      | 'application/xml'         |
      | 'application/rdf+xml'     |

  Scenario: Fetch returns status Bad request when requesting illegal person identifier
    Given path '/person/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be a number or an ORCID'

  Scenario: Fetch returns status Bad request when sending illegal query parameter
    Given path '/person/' + validIdentifier
    And param invalidParam = 'someValue'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'This endpoint does not support query parameters'

  Scenario: Fetch returns status Not found when requesting unknown project identifier
    Given path '/person/' + nonExistingPersonId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingPersonId

  Scenario: Fetch with ORCID returns valid data
    Given path '/person/' + validOrcid
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'

  Scenario: Fetch returns status Not found when requesting unknown ORCID
    Given path '/person/' + nonExistingOrcid
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingOrcid