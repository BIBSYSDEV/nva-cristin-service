Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def nonExistingPersonId = '100000000'
    * def nonExistingEmploymentId = '100000000'
    * def nonValidPersonId = '000A000'
    * def nonValidEmploymentId = '000B0000'
    * def existingPersonId = '538786'
    * def existingEmploymentId = '49272'
    * def invalidToken = 'just-a-invalid-token-for-now'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Delete returns status 401 Forbidden when called without valid token
    Given path '/person/' + nonExistingPersonId +'/employment/' + nonExistingEmploymentId
    * header Authorization = 'Bearer ' + invalidToken
    When method DELETE
    Then status 401

  Scenario: Delete returns status Bad request when requesting illegal person identifier
    Given path '/person/' + nonValidPersonId +'/employment/' + nonExistingEmploymentId
    * header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for \'id\''


  Scenario: Delete returns status Bad request when sending nonvalid employmentId parameter
    Given path '/person/' + nonExistingPersonId +'/employment/' + nonValidEmploymentId
    * header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for \'employmentId\''

  Scenario: Delete returns status Not found when requesting unknown person identifier
    Given path '/person/' + nonExistingPersonId +'/employment/' + nonExistingEmploymentId
    * header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'Upstream returned Not Found. This might occur if person identifier or employment identifier are not found in upstream'

  Scenario: Delete returns status Not found when requesting unknown employmentId
    Given path '/person/' + existingPersonId +'/employment/' + nonExistingEmploymentId
    * header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'Upstream returned Not Found. This might occur if person identifier or employment identifier are not found in upstream'

