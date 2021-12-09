Feature: API tests for Cristin Project retrieve and search

  Background:
    * def testProjectNameSearchTerm = 'univers'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingProjectId = '0'
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE

  Scenario: GET project returns list of empty search results
    Given  path '/project'
    And param query = illegalIdentifier
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == 0

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
    And match response.detail == 'Invalid path parameter for identifier, needs to be a number'

  Scenario: GET returns status Not found when requesting unknown project identifier
    Given path '/project/' + nonExistingProjectId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    And match response.detail == 'https://api.dev.nva.aws.unit.no/' + BASE_PATH + '/project/0'
