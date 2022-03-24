Feature: API tests for Cristin Person Institution Info Update

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
    * def invalidIdentifier = 'hello'
    * def updateFieldsRequest = { phone: '+4799332211', email: null }
    * def unsupportedFieldsRequest = { hello: 'world', lorem: 'ipsum' }
    * def personIdentifier = '515114'
    * def organizationIdentifier = '224.20.0.0'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Update returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier + '/organization/' + organizationIdentifier
    * header Authorization = 'Bearer ' + invalidToken
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier + '/organization/' + organizationIdentifier
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 204 when successful update
    Given path '/person/' + personIdentifier + '/organization/' + organizationIdentifier
    * header Authorization = 'Bearer ' + token
    And request updateFieldsRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when no supported fields in payload
    Given path '/person/' + personIdentifier + '/organization/' + organizationIdentifier
    * header Authorization = 'Bearer ' + token
    And request unsupportedFieldsRequest
    When method PATCH
    Then status 400
    And match response.detail == 'No supported fields in payload, not doing anything'

  Scenario: Update returns status 400 when invalid person identifier
    Given path '/person/' + invalidIdentifier + '/organization/' + organizationIdentifier
    * header Authorization = 'Bearer ' + token
    And request updateFieldsRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Invalid path parameter for \'id\''

  Scenario: Update returns status 400 when invalid organization identifier
    Given path '/person/' + personIdentifier + '/organization/' + invalidIdentifier
    * header Authorization = 'Bearer ' + token
    And request updateFieldsRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Invalid path parameter for \'orgId\''
