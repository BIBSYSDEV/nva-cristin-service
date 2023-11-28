Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def sqlInjectedQuery = 'univers --; select * from users where 1=1'
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE


  Scenario: GET (QUERY) organization with sql injected query returns 400 Bad request
    Given path '/organization'
    And param query = sqlInjectedQuery
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400

  Scenario: GET (QUERY) person with sql injected query returns 400 Bad request
    Given path '/person'
    And param query = sqlInjectedQuery
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400

