Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def testOrganizationNameSearchTerm = 'univers'
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingOrganizationId = '0.1.2.3'
    * def existingOrganizationIdentifier = '20754.0.0.0'
    * def existingOrganizationName = 'Sikt'
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE

  Scenario: GET organization with not found identifier returns empty list of search results
    Given path '/organization'
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
    And match response.detail == 'Parameter \'query\' has invalid value. May only contain alphanumeric characters, dash, comma, period, colon, semicolon and whitespace'

  Scenario: GET returns 404 status Not found when requesting unknown organization identifier
    Given path '/organization/' + nonExistingOrganizationId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404

  Scenario: GET organization with query and result returns list of search results limited to results with position
    Given path '/organization'
    And param query = testOrganizationNameSearchTerm
    And param results = '2'
    And param page = '4'
    And param sort = 'country desc'
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == '#number'
    And match response.hits == '#[2]' // hits array length == 0

  Scenario: GET organization with version 1 for known organization returns list of search results with depth
    Given path '/organization'
    And param query = existingOrganizationName
    And header Accept = 'application/json; version=1'
    When method GET
    Then status 200
    And match response.size != '0'
    And match response.hits == '#array'
    And match response.hits[0].hasPart  == '#present'
    And match response.hits[0].hasPart != '#[0]'

  Scenario: GET organization for known organization returns name in multiple languages as default
    Given path '/organization/' + existingOrganizationIdentifier
    And param depth = 'none'
    When method GET
    Then status 200
    And match response.labels.en == '#present'
    And match response.labels.nb == '#present'

  Scenario: GET organization using version 2023-05-26 returns results
    Given path '/organization'
    And header Accept = 'application/json; version=2023-05-26'
    And param query = testOrganizationNameSearchTerm
    And param results = '2'
    And param page = '4'
    And param sort = 'country desc'
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == '#number'
    And match response.hits == '#[2]' // hits array length == 0

  Scenario: GET organization for known organization using version 2023-05-26 with depth param value none returns data without depth
    Given path '/organization/' + existingOrganizationIdentifier
    And header Accept = 'application/json; version=2023-05-26'
    And param depth = 'none'
    When method GET
    Then status 200
    And match response.labels.en == '#present'
    And match response.labels.nb == '#present'
    And match response.hasPart == '#notpresent'

  Scenario: GET organization for known organization using version 2023-05-26 without depth param returns data with depth
    Given path '/organization/' + existingOrganizationIdentifier
    And header Accept = 'application/json; version=2023-05-26'
    When method GET
    Then status 200
    And match response.labels.en == '#present'
    And match response.labels.nb == '#present'
    And match response.hasPart != '#[0]'

  Scenario: GET organization query wanting whole tree of units returns results enriched with parent and sub units
    Given path '/organization'
    And param query = 'sikt'
    And param fullTree = true
    When method GET
    Then status 200
    And match response.hits[0].hasPart != '#[0]'

  Scenario: GET organization query wanting whole tree of units returns more data
    Given path '/organization'
    And param query = 'sikt'
    And param fullTree = true
    When method GET
    Then status 200
    And match response.hits[0].country = 'NO'
