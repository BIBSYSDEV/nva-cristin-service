Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def illegalOrganizationIdentifier = '3.2.1'
    * def dummyOrganizationIdentifier = '4.3.2.1'
    * def realOrganizationIdentifier = '185.17.6.0'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Get returns status Bad request when requesting illegal organization identifier
    Given path '/organization/'+illegalOrganizationIdentifier+'/persons'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\\d+.){3}\\d+/, e.g. (100.0.0.0)'

  Scenario: Get returns status Bad request when sending illegal parameter
    Given path '/organization/'+dummyOrganizationIdentifier+'/persons'
    And param invalidParam = 'someValue'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid query parameter supplied. Valid parameters: [\'name\', \'page\', \'results\', \'sort\']'

  Scenario: Get returns status OK and context in dummy response
    Given path '/organization/'+dummyOrganizationIdentifier+'/persons'
    And param results = '10'
    When method GET
    Then status 200


  Scenario: Get returns status 400 Bad Request when page is out of scope
    Given path '/organization/'+dummyOrganizationIdentifier+'/persons'
    And param page = '4'
    And param results = '10'
    When method GET
    Then status 400

  Scenario: Get returns status OK and context, next and prev in real organization query response
    Given path '/organization/'+realOrganizationIdentifier+'/persons'
    And param page = '2'
    And param results = '10'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response['nextResults'] == '#present'
    And match response['previousResults'] == '#present'

  Scenario: Get returns OK and results based on name parameter
    Given path '/organization/185.90.0.0/persons'
    And param name = 'daniel'
    When method GET
    Then status 200
    And match response == '#object'
    * string firstHit = response['hits'][0]
    * string secondHit = response['hits'][1]
    And match firstHit.toLowerCase() contains 'daniel'
    And match secondHit.toLowerCase() contains 'daniel'

  Scenario: Get returns OK and results based on sort parameter
    Given path '/organization/185.90.0.0/persons'
    And param sort = 'id desc'
    When method GET
    Then status 200

  Scenario: Get does not return national identification number when not authorized
    Given path '/organization/'+realOrganizationIdentifier+'/persons'
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
