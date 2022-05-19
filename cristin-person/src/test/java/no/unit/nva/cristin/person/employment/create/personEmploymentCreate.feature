Feature: API tests for Creation of Cristin Person Employments

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
     # 'Test Testesen' has id 538786:
    * def cristinTestPersonIdentifier = java.lang.System.getenv('CRISTIN_EMPLOYMENT_TEST_PERSON_IDENTIFIER')
    * def TestPersonEmployment =
    """
    {
      'type': 'https://api.dev.nva.aws.unit.no/position#1087',
      'organization': 'https://api.dev.nva.aws.unit.no/organization/185.90.0.0',
      'startDate': '2020-01-01T00:00:00.000Z',
      'endDate': '2022-01-01T00:00:00.000Z',
      'fullTimeEquivalentPercentage': 80.0
    }
    """
    * def invalidTypePayload =
    """
    {
      'type': 'https://api.dev.nva.aws.unit.no/hello',
      'organization': 'https://api.dev.nva.aws.unit.no/organization/185.90.0.0',
      'startDate': '2020-01-01T00:00:00.000Z',
    }
    """
    * def invalidOrganizationPayload =
    """
    {
      'type': 'https://api.dev.nva.aws.unit.no/position#1087',
      'organization': 'https://api.dev.nva.aws.unit.no/hello',
      'startDate': '2020-01-01T00:00:00.000Z',
    }
    """
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

  Scenario: Create returns status 201 Created for timestamp with zero millis
    Given path '/person/' + cristinTestPersonIdentifier + '/employment/'
    And header Authorization = 'Bearer ' + token
    And request TestPersonEmployment
    When method POST
    Then status 201
    And print response

  Scenario: Create returns status 400 Bad Request when invalid type
    Given path '/person/' + cristinTestPersonIdentifier + '/employment/'
    And header Authorization = 'Bearer ' + token
    And request invalidTypePayload
    When method POST
    Then status 400
    And match response.detail == "Invalid value for field 'type'"

  Scenario: Create returns status 400 Bad Request when invalid organization
    Given path '/person/' + cristinTestPersonIdentifier + '/employment/'
    And header Authorization = 'Bearer ' + token
    And request invalidOrganizationPayload
    When method POST
    Then status 400
    And match response.detail == "Invalid value for field 'organization'"
