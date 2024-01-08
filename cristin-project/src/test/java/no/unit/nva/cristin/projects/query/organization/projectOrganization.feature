Feature: API tests for list Project pr Organization

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
    Given path '/organization/'+illegalOrganizationIdentifier+'/projects'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be organization identifier matching pattern /(?:\\d+.){3}\\d+/, e.g. (100.0.0.0)'

  Scenario: Get returns status Bad request when sending illegal parameter
    Given path '/organization/'+dummyOrganizationIdentifier+'/projects'
    And param invalidParam = 'someValue'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid query parameter supplied. Valid parameters: [\'approval_reference_id\', \'approved_by\', \'biobank\', \'category\', \'creator\', \'cristinId\', \'depth\', \'funding\', \'funding_source\', \'grantId\', \'institution\', \'keyword\', \'language\', \'modified_since\', \'multiple\', \'name\', \'organization\', \'page\', \'participant\', \'project_manager\', \'query\', \'results\', \'sort\', \'status\', \'title\', \'unit\', \'user\']'

  Scenario: Get returns status OK and context in dummy response
    Given path '/organization/'+dummyOrganizationIdentifier+'/projects'
    And param results = '10'
    When method GET
    Then status 200


  Scenario: Get returns status 400 Bad Request when page is out of scope
    Given path '/organization/'+dummyOrganizationIdentifier+'/projects'
    And param page = '4'
    And param results = '10'
    When method GET
    Then status 400

  Scenario: Get returns status OK and context, next and prev in real organization query response
    Given path '/organization/'+realOrganizationIdentifier+'/projects'
    And param page = '2'
    And param results = '2'
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response['nextResults'] == '#present'
    And match response['previousResults'] == '#present'

  Scenario: Query with extended list of parameters and valid values returns OK
    Given path '/organization/'+realOrganizationIdentifier+'/projects'
    And param sort = 'end_date'
    And param project_manager = 'St'
    And param participant = "olav h"
    And param funding_source = 'NRE'
    When method GET
    Then status 200
