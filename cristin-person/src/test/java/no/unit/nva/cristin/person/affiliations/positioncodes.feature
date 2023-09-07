Feature: API tests for Cristin Position Codes

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
    And match response.positions[0].id == '#present'
    And match response.positions[0].labels == '#present'
    And match response.positions[0].enabled == '#present'

