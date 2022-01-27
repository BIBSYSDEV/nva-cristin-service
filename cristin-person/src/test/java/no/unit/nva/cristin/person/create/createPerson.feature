Feature: API tests for Cristin Person creation

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def token = 'just-a-invalid-token-for-now'
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
