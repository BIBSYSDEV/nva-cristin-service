Feature: API tests for Cristin Person fetch

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
    * def samplePersonIdentifier = '515114'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE
#    * print 'token:' + token


  Scenario: Fetch returns unclassified person data when no authorization
    Given path '/person/' + samplePersonIdentifier
    When method GET
    Then status 200
    And print response


  Scenario: Fetch returns classified person data when authorized
    Given path '/person/' + samplePersonIdentifier
    And header Authorization = 'Bearer ' + token
    And request
    When method GET
    Then status 200
    And print response


  Scenario: Fetch returns status Not found when requesting unknown person identifier
    Given path '/person/identityNumber'
    And header Authorization = 'Bearer ' + token
    And request { type : NationalIdentificationNumber, value : '11077941012' }
    When method POST
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == "No match found for supplied payload"
