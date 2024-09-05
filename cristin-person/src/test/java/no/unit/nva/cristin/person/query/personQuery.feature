Feature: API tests for Cristin persons query

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def queryString = 'John'
    * def organizationQuery = 'BI Norwegian Business School'
    * def personIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/person\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Query and receive CORS preflight response
    * configure headers =
  """
    {
      'Origin': 'http://localhost:3000',
      'Accept': 'application/ld+json',
      'Referer': 'Not sure what the value should be yet',
      'Connection': 'keep-alive',
      'Accept-Encoding': 'gzip, deflate, br',
      'Access-Control-Request-Method': 'GET',
      'Access-Control-Request-Headers': 'Content-Type, Authorization'
    }
  """
    Given path '/person/'
    And param name = queryString
    When method OPTIONS
    Then status 200
    And match responseHeaders['Access-Control-Allow-Origin'][0] == '*'
    * def accessControlAllowMethods = responseHeaders['Access-Control-Allow-Methods'][0]
    And match accessControlAllowMethods contains 'GET'
    And match accessControlAllowMethods contains 'OPTIONS'
    * def accessControlAllowHeaders = responseHeaders['Access-Control-Allow-Headers'][0]
    And match accessControlAllowHeaders contains 'Content-Type'
    And match accessControlAllowHeaders contains 'X-Amz-Date'
    And match accessControlAllowHeaders contains 'Authorization'
    And match accessControlAllowHeaders contains 'X-Api-Key'
    And match accessControlAllowHeaders contains 'X-Amz-Security-Token'
    And match accessControlAllowHeaders contains 'Access-Control-Allow-Origin'

  Scenario Outline: Query returns valid data and with correct content negotiation <CONTENT_TYPE>
    * configure headers = { 'Accept': <CONTENT_TYPE> }
    Given path '/person/'
    And param name = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'
    And match response.hits[0].type == 'Person'
    And match response.hits[0].id == '#regex ' + personIdRegex

    Examples:
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |

  Scenario: Query accepts special characters and whitespace
    Given path '/person/'
    And param name = 'Jéan De'La #Luc'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'

  Scenario Outline: Query with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/person/'
    And param name = queryString
    When method GET
    Then status 415
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Unsupported Media Type'
    And match response.status == 415
    And match response.detail == <UNACCEPTABLE_CONTENT_TYPE> + ' contains no supported Accept header values. Supported values are: application/json; charset=utf-8, application/ld+json'
    And match response.requestId == '#notnull'

    Examples:
      | UNACCEPTABLE_CONTENT_TYPE |
      | 'image/jpeg'              |
      | 'application/xml'         |
      | 'application/rdf+xml'     |

  Scenario: Query with bad parameter returns Bad Request
    Given path '/person/'
    And param notValidParam = 'someValue'
    When method GET
    Then status 400
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == "Invalid query parameter supplied. Valid parameters: ['name', 'organization', 'organizationFacet', 'page', 'results', 'sectorFacet', 'sort', 'verified']"
    And match response.requestId == '#notnull'

  Scenario Outline: Query with correct parameters but bad values returns Bad Request
    Given path '/person/'
    And param name = queryString
    And param <VALID_PARAM> = <INVALID_PARAM_VALUE>
    When method GET
    Then status 400
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == '#present'
    And match response.requestId == '#notnull'

    Examples:
      | VALID_PARAM | INVALID_PARAM_VALUE |
      | 'page'      | '-1'                |
      | 'results'   | '-1'                |
      | 'page'      | 'hello'             |
      | 'results'   | 'hello'             |

  Scenario: Query returns correct pagination values and URIs
    Given path '/person/'
    And param name = queryString
    And param results = '3'
    And param page = '2'
    When method GET
    Then status 200
    * def nextResultsPath = CRISTIN_BASE + '/person?name=' + queryString + '&page=3&results=3'
    * def previousResultsPath = CRISTIN_BASE + '/person?name=' + queryString + '&page=1&results=3'
    And match response.nextResults == nextResultsPath
    And match response.previousResults == previousResultsPath
    And match response.firstRecord == 4
    And match response.hits == '#[3]'

  Scenario: Query returns correct pagination values and URIs when organization is added to query
    Given path '/person'
    And param name = queryString
    And param organization = organizationQuery
    And param results = '3'
    And param page = '2'
    When method GET
    Then status 200
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'
    And match response.hits[0].type == 'Person'

  Scenario: Get does not return national identification number when not authorized
    Given path '/person/'
    And param name = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.hits[0].type == 'Person'
    And match response.hits[0].id == '#present'
    And match response.hits[0].NationalIdentificationNumber != '#present'
    * string identifiers = response['hits'][0].identifiers
    And match identifiers contains 'CristinIdentifier'
    And match identifiers !contains 'NationalIdentificationNumber'

  Scenario: Query returns unverified persons when setting param verified to false
    Given path '/person/'
    And param name = 'daniel'
    And param verified = 'false'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.hits[0].verified == false

  Scenario: Query contains correct searchString matching supplied params and default params
    Given path '/person/'
    And param name = 'daniel'
    And param organization = 'uio'
    And param sort = 'name desc'
    When method GET
    Then status 200
    * string searchString = response.searchString
    And match searchString contains 'organization'
    And match searchString contains 'name'
    And match searchString contains 'page'
    And match searchString contains 'results'
    And match searchString contains 'sort'

  Scenario: Query returns aggregations when requesting version with aggregations
    Given path '/person/'
    And header Accept = 'application/json; version=2023-11-03-aggregations'
    And param name = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response.aggregations == '#present'
    And match response.aggregations.organizationFacet[0].id == '#present'
    And match response.aggregations.organizationFacet[0].key == '#present'
    And match response.aggregations.organizationFacet[0].count == '#present'
    And match response.aggregations.organizationFacet[0].labels == '#present'

  Scenario: Query returns open data only when requesting version with aggregations
    Given path '/person/'
    And header Accept = 'application/json; version=2023-11-03-aggregations'
    And param name = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response.hits[0].NationalIdentificationNumber != '#present'
    * string identifiers = response['hits'][0].identifiers
    And match identifiers contains 'CristinIdentifier'
    And match identifiers !contains 'NationalIdentificationNumber'
    And match response.aggregations == '#present'
    And match response.aggregations.organizationFacet[0].id == '#present'

  Scenario: Query returns all results when calling endpoint with no params
    Given path '/person/'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'
    And match response.hits[0].type == 'Person'
    And match response.hits[0].id == '#regex ' + personIdRegex
