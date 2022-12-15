Feature: API tests for Cristin Funding Sources list

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def fundingSourcesIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/funding-sources'
    * def fundingSourceIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/funding-sources\/.+'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch and receive CORS preflight response
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
    Given path '/funding-sources'
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

  Scenario Outline: List returns valid data and with correct content negotiation <CONTENT_TYPE>
    * configure headers = { 'Accept': <CONTENT_TYPE> }
    Given path '/funding-sources'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + fundingSourcesIdRegex
    And match response.type == 'FundingSources'
    And match response.sources[0].type == 'FundingSource'
    And match response.sources[0].id == '#regex ' + fundingSourceIdRegex
    And match response.sources[0].identifier == '#present'
    And match response.sources[0].name == '#present'

    Examples:
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |

