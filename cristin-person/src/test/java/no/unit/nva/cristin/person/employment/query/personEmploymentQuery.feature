Feature: API tests for Cristin Person Employments

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('TESTUSER_FEIDE_ID')
    * def password = java.lang.System.getenv('TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoUserpoolId, cognitoClientAppId)
    * def invalidToken = 'just-a-invalid-token-for-now'
    * def personIdentifier = '515114'
    * def invalidIdentifier = 'hello'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + invalidToken
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Fetch returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier + '/employment/'
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Fetch returns status 400 when invalid identifier
    Given path '/person/' + invalidIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 400
    And match response.detail == 'Invalid path parameter for \'id\''

  Scenario Outline: Query returns valid data and with correct content negotiation <CONTENT_TYPE>
    Given path '/person/' + personIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + token
    * header Accept = <CONTENT_TYPE>
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'
    And print response

    Examples:
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |
