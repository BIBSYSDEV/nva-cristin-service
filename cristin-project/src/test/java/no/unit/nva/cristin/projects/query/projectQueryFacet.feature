Feature: API tests for Cristin projects facets query

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath

    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario Outline: Query returns results with aggregations with aggregation delimiter both encoded and non-encoded
    Given path '/project/'
    And header Accept = 'application/json; version=2023-11-03-aggregations'
    And param name = 'covid'
    And param categoryFacet = <CATEGORY_PARAM>
    When method GET
    Then status 200
    And match response == '#object'
    And match response.hits[0] == '#present'
    And match response.hits[0].id == '#present'
    And match response.aggregations.categoryFacet[0].id == '#present'

    Examples:
      | CATEGORY_PARAM              |
      | 'APPLIEDRESEARCH%2CINHOUSE' |
      | 'APPLIEDRESEARCH,INHOUSE'   |
