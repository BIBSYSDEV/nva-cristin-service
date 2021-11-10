Feature: API tests for Cristin Project retrieve and search

  Background:
    * def SERVER_URL = 'https://api.dev.nva.aws.unit.no'
    * def CRISTIN_BASE = SERVER_URL + '/karate-cristin'
    * def testProjectNameSearchTerm = 'univers'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingProjectId = '0'
    Given url CRISTIN_BASE


#  Scenario: GET organization returns list of search results
#    Given  path '/test-cristin/project'
#    When method GET
#    Then status 200
#    And match response.hits == '#array'
#    And match response.total == '#number'

  Scenario: GET resources with query returns list of search results
    Given path '/project'
    And param query = testProjectNameSearchTerm
    When method GET
    Then status 200
    And match response.firstRecord == 1

  Scenario: GET resources with query and result returns list of search results limited to results with position
    Given path '/project'
    And param query = testProjectNameSearchTerm
    And param results = '2'
    And param page = '4'
    When method GET
    Then status 200
    And match response.hits == '#[2]' // hits array length == 2
    And match response.firstRecord == 7

  Scenario: GET returns status Bad request when requesting illegal project identifier
    Given path '/project/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be a number....'

  Scenario: GET returns status Not found when requesting unknown project identifier
    Given path '/project/' + nonExistingProjectId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'https://api.dev.nva.aws.unit.no/karate-cristin/project/0'
