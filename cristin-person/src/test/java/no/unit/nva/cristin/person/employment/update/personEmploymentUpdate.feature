Feature: API tests for Cristin Person Employment Update

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def simple_user_name = java.lang.System.getenv('SIMPLE_TESTUSER_ID')
    * def simple_user_password = java.lang.System.getenv('SIMPLE_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def simpleUserToken = tokenGenerator.loginUser(simple_user_name, simple_user_password, cognitoClientAppId)
    * def invalidToken = 'just-a-invalid-token-for-now'
    * def personIdentifier = '515114'
    * def employmentIdentifier = '206517'
    Given url CRISTIN_BASE

  Scenario: Update returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    * header Authorization = 'Bearer ' + invalidToken
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 403 Forbidden when not authorized
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    And header Authorization = 'Bearer ' + simpleUserToken
    When method PATCH
    Then status 403
    And match response.title == 'Forbidden'

  Scenario: Update returns bad request when invalid position code
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidPositionCodeRequest =
    """
    {
      "type": "https://api.dev.nva.aws.unit.no/hello"
    }
    """
    And request invalidPositionCodeRequest
    When method PATCH
    Then status 400

  Scenario: Update returns bad request when invalid affiliation
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidAffiliationRequest =
    """
    {
      "organization": "https://api.dev.nva.aws.unit.no/hello"
    }
    """
    And request invalidAffiliationRequest
    When method PATCH
    Then status 400

  Scenario: Update returns bad request when invalid date
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidDateRequest =
    """
    {
      "startDate": "hello"
    }
    """
    And request invalidDateRequest
    When method PATCH
    Then status 400

  Scenario: Update returns bad request when invalid full time percentage
    Given path '/person/' + personIdentifier + '/employment/' + employmentIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidFullTimeRequest =
    """
    {
      "fullTimePercentage": "hello"
    }
    """
    And request invalidFullTimeRequest
    When method PATCH
    Then status 400
