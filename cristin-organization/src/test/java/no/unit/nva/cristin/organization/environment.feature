Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def illegalIdentifier = 'illegalIdentifier'
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE


  Scenario: Print environment
    * print 'basePath =', basePath
    * print 'CRISTIN_BASE =', CRISTIN_BASE
    * print 'domainName =', domainName
    * match CRISTIN_BASE == 'https://api.dev.nva.aws.unit.no/cristin-karate-tests'
#  Scenario: GET organization returns list of search results
#    Given path '/organization'
#    And param query = illegalIdentifier
#    When method GET
#    Then status 200
#    And match response.hits == '#array'
#    And match response.size == 0
#
#  Scenario: GET organization returns list of search results
#    Given path '/organization/185.53.18.14'
#    When method GET
#    Then status 200
#    * print response
