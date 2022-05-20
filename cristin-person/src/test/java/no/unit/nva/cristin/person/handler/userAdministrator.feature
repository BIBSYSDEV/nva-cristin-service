Feature: API tests for Cristin Person fetch

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
    * def samplePersonIdentifier = '515114'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch returns classified person data when authenticated and authorized
    Given path '/person/' + samplePersonIdentifier
    And header Authorization = 'Bearer ' + token
    And request
    When method GET
    Then status 200
    And response.NationalIdentificationNumber == '#present'

  Scenario: Fetch returns unclassified person data when authenticated but not authorized
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer ' + simpleUserToken
    When method GET
    Then status 200
    And response.NationalIdentificationNumber != '#present'

  Scenario: Fetch returns unclassified person data when not authenticated nor authorized
    Given path '/person/' + samplePersonIdentifier
    When method GET
    Then status 200
    And response.NationalIdentificationNumber != '#present'

  Scenario: Fetch returns 200 OK and no NationalIdentificationNumber when token is invalid
    Given path '/person/' + samplePersonIdentifier
    * header Authorization = 'Bearer and.just-a.silly-text-for-token'
    When method GET
    Then status 200
    And response.NationalIdentificationNumber == '#present'

  Scenario: Fetch returns status Not found when requesting unknown person identifier
    Given path '/person/identityNumber'
    And header Authorization = 'Bearer ' + token
    And request { type : NationalIdentificationNumber, value : '11077941012' }
    When method POST
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == "No match found for supplied payload"
