Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def testOrganizationNameSearchTerm = 'univers'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingOrganizationId = '0.1.2.3'
    * def existingOrganizationIdentifier = '185.90.0.0'
    * def existingOrganizationName = 'Universitetet i Oslo'
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
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
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\\d+.){3}\\d+/, e.g. (100.0.0.0)'

  Scenario: GET returns 400 status Bad request when requesting illegal organization identifier
    Given path '/organization/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\\d+.){3}\\d+/, e.g. (100.0.0.0)'

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
    And match response.detail == 'The resource \'' + CRISTIN_BASE + '/organization/0.1.2.3\' cannot be dereferenced'

  Scenario: GET organization with query and result returns list of search results limited to results with position
    Given path '/organization'
    And param query = testOrganizationNameSearchTerm
    And param results = '2'
    And param page = '4'
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == '#number'
    And match response.hits == '#[2]' // hits array length == 0

  Scenario: GET organization for known organization returns list of search results with depth
    Given  path '/organization'
    And param query = existingOrganizationName
    When method GET
    Then status 200
    And match response.size != '0'
    And match response.hits == '#array'
    And match response.hits[0].partOf == '#notpresent'
    And match response.hits[0].hasPart  == '#present'

  Scenario: GET organization for known organization returns list of search results without depth
    Given  path '/organization'
    And param query = existingOrganizationIdentifier
    And param depth = 'none'
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.hits[0].partOf == '#notpresent'
    And match response.hits[0].hasPart  == '#notpresent'
