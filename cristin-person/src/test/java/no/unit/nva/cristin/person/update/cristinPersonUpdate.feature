Feature: API tests for Cristin Person Update

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
    * def personIdentifier = '1135903'

    * def updateFieldsRequest =
    """
    {
      'firstName': 'Helge',
      'lastName': 'Pettersen',
      'preferredFirstName': null
    }
    """
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Update returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + invalidToken
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 401 Unauthorized when not authorized because of getPersonCristinId
    Given path '/person/' + personIdentifier
    And header Authorization = 'Bearer ' + simpleUserToken
    When method PATCH
    Then status 401
    And match response.title == 'Unauthorized'

  Scenario: Update returns status 204 when valid payload
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    And request updateFieldsRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 204 when sending null orcid to erase
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def validRequest =
    """
    {
      'orcid': null
    }
    """
    And request validRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when invalid orcid
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'orcid': '1234'
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'ORCID is not valid'

  Scenario: Update returns status 400 when trying to erase non nullable name
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'firstName': null
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Field firstName can not be erased'

  Scenario: Update returns status 400 when trying to erase non nullable reserved
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'reserved': null
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Reserved field can only be set to true if present'

  Scenario: Update returns status 400 when trying to set reserved to unsupported value
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'reserved': false
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Reserved field can only be set to true if present'

  Scenario: Update returns status 400 when no supported values present
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'invalidField': 'invalidValue'
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'No supported fields in payload, not doing anything'

  Scenario: Update returns status 204 when sending valid payload containing employments
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': [
        {
          'type': 'https://api.dev.nva.aws.unit.no/cristin/position#1087',
          'organization': 'https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0',
          'startDate': '2008-01-01T00:00:00Z',
          'endDate': '2025-12-31T00:00:00Z',
          'fullTimeEquivalentPercentage': 100
        }
      ]
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 204 when removing all employments
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': []
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when sending invalid employments payload
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': [
        {
          'hello': 'world'
        }
      ]
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 400
