Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingPersonId = '999999'
    * def validIdentifier = '738'
    * def validOrcid = '0009-0003-1236-0353'
    * def nonExistingOrcid = '1111-1111-1111-1111'
    * def personIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/person\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    * def admin_username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def admin_password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def simple_username = java.lang.System.getenv('SIMPLE_TESTUSER_ID')
    * def simple_password = java.lang.System.getenv('SIMPLE_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def admin_token = tokenGenerator.loginUser(admin_username, admin_password, cognitoClientAppId)
    * def simple_user_token = tokenGenerator.loginUser(simple_username, simple_password, cognitoClientAppId)
    * def samplePersonIdentifier = '515114'

    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch and receive CORS preflight response
    * configure headers =
  """
    {
      'Origin': 'http://localhost:3000',
      'Accept': 'application/ld+json',
      'Referer': 'Not sure what the value should be yet',
      'Connection': 'keep-alive',
      'Accept-Encoding': 'gzip, deflate, br',
      'Access-Control-Request-Method': 'GET',
      'Access-Control-Request-Headers': 'Content-Type, Authorization'
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
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |

  Scenario Outline: Fetch with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/person/' + validIdentifier
    When method GET
    Then status 415
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Unsupported Media Type'
    And match response.status == 415
    And match response.detail == <UNACCEPTABLE_CONTENT_TYPE> + ' contains no supported Accept header values. Supported values are: application/json; charset=utf-8, application/ld+json'
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
    * def uri = 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingPersonId
    And match response.detail == "The requested resource '" + uri + "' was not found"

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
    * def uri = 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingOrcid
    And match response.detail == "The requested resource '" + uri + "' was not found"

  Scenario: Fetch does not return employments for person when missing required rights
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer ' + simple_user_token
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'
    And match response.employments != '#present'

  Scenario: Get does not return national identification number when not authorized
    * def samplePersonIdentifier = '515114'
    Given path '/person/' + samplePersonIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response.type == 'Person'
    And match response.id == '#present'
    And match response.NationalIdentificationNumber != '#present'
    * string identifiers = response.identifiers
    And match identifiers contains 'CristinIdentifier'
    And match identifiers !contains 'NationalIdentificationNumber'

  Scenario: Get returns supported fields in payload
    * def samplePersonIdentifier = '854279'
    Given path '/person/' + samplePersonIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response.verified == '#present'
    And match response.verified == true
    And match response.keywords[0].type == '#present'
    And match response.keywords[0].label == '#present'

  Scenario: Fetch returns employments for person when having required rights
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer ' + admin_token
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'
    And match response.employments == '#present'
    And match response.employments[0].organization == '#present'

  Scenario: Get returns more supported fields in payload for another user
    * def sampleAnotherPersonIdentifier = '1684653'
    Given path '/person/' + sampleAnotherPersonIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response.contactDetails == '#present'
    And match response.contactDetails.telephone == '11223344'
    And match response.contactDetails.email == 'test@example.org'
    And match response.contactDetails.webPage == 'www.example.org'
    And match response.place == '#present'
    And match response.place['nb'] == 'Min institusjon'
    And match response.place['en'] == 'My institution'
    And match response.collaboration == '#present'
    And match response.collaboration['nb'] == 'Mine samarbeidende institusjoner'
    And match response.countries == '#present'
    And match response.countries[0].type == '#present'
    And match response.countries[0].label == '#present'
    And match response.countries[0].label == '#object'
    And match response.awards == '#present'
    And match response.awards[0].name == 'My first award'
    And match response.awards[0].year == 2014
    And match response.awards[0].type.type == 'ResearchDissemination'
    And match response.awards[0].type.label == '#present'
    And match response.awards[0].distribution.type == 'National'
    And match response.awards[0].distribution.label == '#present'
    And match response.awards[0].affiliation == '#present'
