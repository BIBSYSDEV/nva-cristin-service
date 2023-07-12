Feature: API tests for Cristin Project authorized fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def projectIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/project\/[0-9]+'
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch project requiring authorization because unpublishable is viewable by project creator
    Given path '/project/14336663'
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response == '#object'
    And match response.id == '#regex ' + projectIdRegex
    And match response.publishable == false
