Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def illegalOrganizationIdentifier = '3.2.1'
    * def organizationIdentifier = '4.3.2.1'
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
    Given path '/organization/'+organizationIdentifier+'/persons'
    And param invalidParam = 'someValue'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid query param supplied. Valid ones are 'page' and 'results''

  Scenario: Get returns status OK and context in dummy response
    Given path '/organization/'+organizationIdentifier+'/persons'
    And param page = '4'
    And param results = '10'
    When method GET
    Then status 200
