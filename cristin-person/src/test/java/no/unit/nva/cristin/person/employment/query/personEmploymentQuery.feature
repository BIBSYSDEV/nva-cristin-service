Feature: API tests for Cristin Person Employments

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def invalidToken = 'just-a-invalid-token-for-now'
    * def personIdentifier = '515114'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier + '/employment/'
    * header Authorization = 'Bearer ' + invalidToken
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Fetch returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier + '/employment/'
    When method GET
    Then status 401
    And match response.message == 'Unauthorized'