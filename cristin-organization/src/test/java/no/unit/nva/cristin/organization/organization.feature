Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def SERVER_URL = 'https://api.dev.nva.aws.unit.no'
    * def testOrganizationNameSearchTerm = 'univers'
    * def CRISTIN_BASE = SERVER_URL + '/karate-cristin'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingOrganizationId = '0.1.2.3'
    Given url CRISTIN_BASE

  Scenario: GET organization returns list of search results
    Given  path '/organization'
    And param query = illegalIdentifier
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == 0

  Scenario: GET returns 400 status Bad request when path parameter identifier is missing
    Given path '/organization/+'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\d+.){3}\d+/, e.g. (100.0.0.0)'

  Scenario: GET returns 400 status Bad request when requesting illegal organization identifier
    Given path '/organization/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\d+.){3}\d+/, e.g. (100.0.0.0)'

  Scenario: GET returns 400 status Bad request when requesting without any query parameter
    Given path '/organization/'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Parameter \'query\' is missing or invalid. May only contain alphanumeric characters, dash, comma, period and whitespace'

  Scenario: GET returns 404 status Not found when requesting unknown organization identifier
    Given path '/organization/' + nonExistingOrganizationId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'The URI "https://api.dev.nva.aws.unit.no/karate-cristin/organization/0.1.2.3" cannot be dereferenced'

#  Scenario: GET organization with query and result returns list of search results limited to results with position
#    Given path '/organization'
#    And param query = testOrganizationNameSearchTerm
#    And param results = '2'
#    And param page = '4'
#    When method GET
#    Then status 200
#    And match response.hits == '#array'
#    And match response.total == '#number'
#    And match response.hits == '#[0]' // hits array length == 0
#    And match response.firstRecord == 0
