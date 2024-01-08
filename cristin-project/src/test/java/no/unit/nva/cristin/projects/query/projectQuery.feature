Feature: API tests for Cristin projects query

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def queryString = 'covid'
    * def projectIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/project\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    * def EXPECTED_JSON_MEDIA_TYPE = 'application/json; charset=utf-8'
    * def organizationId = 'https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0'

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
    Given path '/project/'
    And param query = queryString
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
    Given path '/project/'
    And param query = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'
    And match response.hits[0].type == 'Project'
    And match response.hits[0].id == '#regex ' + projectIdRegex
    And match response.hits[0].title == '#present'

    Examples:
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |

  Scenario: Query accepts special characters and whitespace
    Given path '/project/'
    And param query = 'Kåre G'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#present'
    And match response.size == '#present'
    And match response.hits == '#present'

  Scenario Outline: Query with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/project/'
    And param query = queryString
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
    Given path '/project/'
    And param notValidParam = 'someValue'
    When method GET
    Then status 400
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid query parameter supplied. Valid parameters: [\'approval_reference_id\', \'approved_by\', \'biobank\', \'category\', \'creator\', \'cristinId\', \'depth\', \'funding\', \'funding_source\', \'grantId\', \'institution\', \'keyword\', \'language\', \'modified_since\', \'multiple\', \'name\', \'organization\', \'page\', \'participant\', \'project_manager\', \'query\', \'results\', \'sort\', \'status\', \'title\', \'unit\', \'user\']'
    And match response.requestId == '#notnull'

  Scenario Outline: Query with correct parameters but bad values returns Bad Request
    Given path '/project/'
    And param query = queryString
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

  Scenario: Query with missing query parameter do returns results
    Given path '/project/'
    And method GET
    Then status 200
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == EXPECTED_JSON_MEDIA_TYPE
    And match response.nextResults == '#present'
    And match response.previousResults == '#present'
    And match response.firstRecord == '#present'
    And match response.hits == '#[5]'

  Scenario: Query returns correct pagination values and URIs
    Given path '/project/'
    And param query = queryString
    And param results = '5'
    And param page = '2'
    When method GET
    Then status 200
    * def nextResultsPath = CRISTIN_BASE + '/project?page=3&title=' + queryString + '&results=5'
    * def previousResultsPath = CRISTIN_BASE + '/project?page=1&title=' + queryString + '&results=5'
    And match response.nextResults == nextResultsPath
    And match response.previousResults == previousResultsPath
    And match response.firstRecord == 6
    And match response.hits == '#[5]'

  Scenario: Query with grant id returns correct project
    Given path '/project/'
    * def grantId = '226139'
    And param query = grantId
    When method GET
    Then status 200
    And match response.hits == '#[1]'
    * def matchingProject = '432742'
    And match response.hits[0].id contains matchingProject
    And match response.hits[0].title == '#present'


  Scenario: Query accepts organization uri as query parameter
    Given path '/project/'
    And param query = queryString
    And param organization = organizationId
    When method GET
    Then status 200

  Scenario: Query with illegal organizationId  returns 400 Bad request
    Given path '/project/'
    * def illegalOrganizationId = 'htttps:/api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0'
    And param query = queryString
    And param organization = illegalOrganizationId
    When method GET
    Then status 400

  Scenario Outline: Query accepts status parameter value case independent
    Given path '/project/'
    And param query = queryString
    And param status = <SAMPLE_STATUS>
    When method GET
    Then status 200

    Examples:
      | SAMPLE_STATUS |
      | 'concluded' |
      | 'notstarted' |
      | 'active' |
      | 'CONCLUDED' |
      | 'NOTSTARTED' |
      | 'ACTIVE' |

  Scenario: Query with extended list of parameters and valid values returns OK
    Given path '/project/'
    And param query = queryString
    And param sort = 'end_date'
    And param biobank = '234567'
    And param project_manager = 'St'
    And param participant = "olav h"
    When method GET
    Then status 200


  Scenario: Query with valid parameter key but bad value not validated returns Bad Request from upstream
    Given path '/project/'
    And param query = queryString
    And param sort = 'notADateRange'
    When method GET
    Then status 400
    * def contentType = responseHeaders['Content-Type'][0]
    And match contentType == PROBLEM_JSON_MEDIA_TYPE
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Upstream returned 400 (Bad Request). That might indicate bad query parameters'

  Scenario: Query returns aggregations when requesting version with aggregations
    Given path '/project/'
    And header Accept = 'application/json; version=2023-11-03-aggregations'
    And param query = queryString
    When method GET
    Then status 200
    And match response == '#object'
    And match response.aggregations == '#present'
    And match response.aggregations.sectorFacet[0].id == '#present'
    And match response.aggregations.sectorFacet[0].key == '#present'
    And match response.aggregations.sectorFacet[0].count == '#present'
    And match response.aggregations.sectorFacet[0].labels == '#present'
