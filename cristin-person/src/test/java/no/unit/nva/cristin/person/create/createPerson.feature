Feature: API tests for Cristin Person creation

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def token = 'just-a-invalid-token-for-now'
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.utils.CognitoUtil')
    * def administratorToken = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def sampleSimplePerson =
     """
    {
      'identifiers': [
        {
          'type': 'NationalIdentificationNumber',
          'value': '12345678901'
        }
      ],
      'names': [
        {
          'type': 'PreferredFirstName',
          'value': 'Ola'
        },
        {
          'type': 'PreferredLastName',
          'value': 'Nordmann'
        },
        {
          'type': 'FirstName',
          'value': 'Kjell Ola'
        },
        {
          'type': 'LastName',
          'value': 'Nordmann'
        }
      ]
    }
    """
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Create person returns status 401 Unauthorized when invalid token
    Given path '/person/'
    * header Authorization = 'Bearer ' + token
    When method POST
    Then status 401

  Scenario: Create person returns status 401 Unauthorized when missing token
    Given path '/person/'
    When method POST
    Then status 401

# Handler validates NIN after access check -> requester i allowed to create person
# NIN must be unique so test cannot create new user each time as we have no 'delete-user'-function
  Scenario: Create person returns status 400 Bad Request when user is authenticated but NIN is invalid
    Given path '/person/'
    * header Authorization = 'Bearer ' + administratorToken
    And request sampleSimplePerson
    When method POST
    Then status 400
    And response.detail = 'Required field NationalIdentificationNumber is not valid'

  Scenario: Create person returns status 401 Unauthorized for valid payload but unauthorized user
    Given path '/person/'
    * header Authorization = 'Bearer ' + token
    And request sampleSimplePerson
    When method POST
    Then status 401

  Scenario: Create person returns status 401 Unauthorized for valid payload but unauthenticated user
    Given path '/person/'
    And request sampleSimplePerson
    When method POST
    Then status 401

