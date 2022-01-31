Feature: API tests for Cristin Person Institution Info

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def token = 'just-a-invalid-token-for-now'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch returns status 401 Unauthorized when invalid token
    Given path '/person/738/organization/185'
    * header Authorization = 'Bearer ' + token
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Fetch returns status 401 Unauthorized when missing token
    Given path '/person/738/organization/185'
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'
