Feature: API tests for Creation of Cristin Person Employments

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('TESTUSER_FEIDE_ID')
    * def password = java.lang.System.getenv('TESTUSER_PASSWORD')
    * def simple_user_name = java.lang.System.getenv('SIMPLE_TESTUSER_FEIDE_ID')
    * def simple_user_password = java.lang.System.getenv('SIMPLE_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoUserpoolId, cognitoClientAppId)
    * def simpleUserToken = tokenGenerator.loginUser(simple_user_name, simple_user_password, cognitoUserpoolId, cognitoClientAppId)
    * def invalidToken = 'just-a-invalid-token-for-now'
    * def personIdentifier = '515114'
    Given url CRISTIN_BASE

  Scenario: Create returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + invalidToken
    When method POST
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Create returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier + '/employment/'
    When method POST
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Create returns status 403 Forbidden when not authorized
    Given path '/person/' + personIdentifier + '/employment/'
    And header Authorization = 'Bearer ' + simpleUserToken
    When method POST
    Then status 403
    And match response.title == 'Forbidden'

  Scenario: Create returns status 201 when passing auth check
    Given path '/person/' + personIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + token
    When method POST
    Then status 201