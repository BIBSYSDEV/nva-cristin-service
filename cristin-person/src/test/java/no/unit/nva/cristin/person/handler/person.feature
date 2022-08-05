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
    * def username = java.lang.System.getenv('SIMPLE_TESTUSER_ID')
    * def password = java.lang.System.getenv('SIMPLE_TESTUSER_PASSWORD')
    * def admin_user_name = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def admin_user_password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def adminToken = tokenGenerator.loginUser(admin_user_name, admin_user_password, cognitoClientAppId)
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
    * header Authorization = 'Bearer ' + token
    When method GET
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

  Scenario Outline: Fetch with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/person/' + validIdentifier
    * header Authorization = 'Bearer ' + token
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
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be a number or an ORCID'

  Scenario: Fetch returns status Bad request when sending illegal query parameter
    Given path '/person/' + validIdentifier
    And param invalidParam = 'someValue'
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'This endpoint does not support query parameters'

  Scenario: Fetch returns status Not found when requesting unknown project identifier
    Given path '/person/' + nonExistingPersonId
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    * def uri = 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingPersonId
    And match response.detail == "The requested resource '" + uri + "' was not found"

  Scenario: Fetch with ORCID returns valid data
    Given path '/person/' + validOrcid
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'

  Scenario: Fetch returns status Not found when requesting unknown ORCID
    Given path '/person/' + nonExistingOrcid
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    * def uri = 'https://api.dev.nva.aws.unit.no/' + basePath + '/person/' + nonExistingOrcid
    And match response.detail == "The requested resource '" + uri + "' was not found"

  Scenario: Fetch returns employments for given person when having required rights
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer ' + adminToken
    When method GET
    Then status 200
    And print response
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'
    And match response.employments == '#present'
    And assert response.employments.length > 0

  Scenario: Fetch does not return employments for person when missing required rights
    Given path '/person/' + samplePersonIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'
    And match response.employments == '#present'
    And assert response.employments.length == 0

  Scenario: Fetch does not return employments for person when logged in but missing required rights
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + personIdRegex
    And match response.type == 'Person'
    And match response.employments == '#present'
    And assert response.employments.length == 0
