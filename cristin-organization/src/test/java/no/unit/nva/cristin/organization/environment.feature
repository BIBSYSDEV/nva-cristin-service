Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def CRISTIN_BASE = baseUrl + 'cristin-karate-tests'
    * def illegalIdentifier = 'illegalIdentifier'
    Given url CRISTIN_BASE


  Scenario: Print environment
    * print 'baseUrl=', baseUrl
    * print 'CRISTIN_BASE=', CRISTIN_BASE
#    * print 'url=', url

  Scenario: GET organization returns list of search results
    Given path '/organization'
    And param query = illegalIdentifier
    When method GET
    Then status 200
    And match response.hits == '#array'
    And match response.size == 0

  Scenario: GET organization returns list of search results
    Given path '/organization/185.53.18.14'
    When method GET
    Then status 200
    * print response
