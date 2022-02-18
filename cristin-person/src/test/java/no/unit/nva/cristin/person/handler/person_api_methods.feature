Feature: API tests for Cristin Person fetch

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario Outline: Unsupported method <METHOD> returns 403 Forbidden
    Given path '/person/'
    When method <METHOD>
    Then status 403

    Examples:
      | METHOD  |
      | HEAD    |
      | PUT     |
      | DELETE  |
      | CONNECT |

  Scenario: Unsupported method TRACE returns 405
    Given path '/person/'
    When method TRACE
    Then status 405

  Scenario: Supported method GET returns 400 bad request when missing query parameter
    Given path '/person/'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'

  Scenario: Supported method POST returns 401 Unauthorized whe no authentication token
    Given path '/person/'
    When method POST
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Supported method PATCH returns 403 Unauthorized when no authentication token
    Given path '/person/'
    When method PATCH
    Then status 403
    And match response.message == 'Missing Authentication Token'


  Scenario: Supported method OPTIONS returns 200 and 'Access-Control-Allow-Methods' header
    Given path '/person/'
    When method OPTIONS
    Then status 200
    * def accessControlAllowMethods = responseHeaders['Access-Control-Allow-Methods'][0]
    And match accessControlAllowMethods contains 'GET'
    And match accessControlAllowMethods contains 'OPTIONS'

