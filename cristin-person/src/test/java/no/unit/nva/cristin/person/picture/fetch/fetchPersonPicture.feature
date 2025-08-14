Feature: API tests for Cristin Person Picture Fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def personIdentifier = '1862305'
    * def nonExistingPerson = '111222333'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch returns status 200 and picture in base64 format
    Given path '/person/' + personIdentifier + '/picture/'
    When method GET
    Then status 200
    And match response.base64Data == '#present'

  Scenario: Fetch returns status 404 when person does not have picture
    Given path '/person/' + nonExistingPerson + '/picture/'
    When method GET
    Then status 404
