Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE

  Scenario: GET organization returns list of search results
    Given  path '/position'
    When method GET
    Then status 200
    And match response.positions == '#array'
    And  response.size >= 1

