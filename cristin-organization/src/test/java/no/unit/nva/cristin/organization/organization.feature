Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def SERVER_URL = 'https://api.dev.nva.aws.unit.no'
    * def testOrganizationNameSearchTerm = 'univers'
    * def CRISTIN_BASE = SERVER_URL + '/test-cristin'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingOrganizationId = '0'
    Given url CRISTIN_BASE

  Scenario: GET organization returns list of search results
    Given  path '/organization'
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == '#number'

  Scenario: GET organization with query returns list of search results
    Given path '/organization'
    And param query = testOrganizationNameSearchTerm
    When method GET
    Then status 200
    And match response.hits == '#[10]' // hits array length == 6
#    And match response.total == 6
    And match response.firstRecord == 1


  Scenario: GET organization with query and result returns list of search results limited to results with position
    Given path '/organization'
    And param query = testOrganizationNameSearchTerm
    And param results = '2'
    And param page = '4'
    When method GET
    Then status 200
    And match response.hits == '#[2]' // hits array length == 2
    And match response.firstRecord == 7

  Scenario: GET returns status Bad request when requesting illegal organization identifier
    Given path '/organization/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for id, needs to be a number'

  Scenario: GET returns status Not found when requesting unknown organization identifier
    Given path '/organization/' + nonExistingOrganizationId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'https://api.dev.nva.aws.unit.no/test-cristin/project/0'
