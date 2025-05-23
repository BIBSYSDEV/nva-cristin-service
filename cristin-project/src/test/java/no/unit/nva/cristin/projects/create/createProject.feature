Feature: API tests for Cristin Project retrieve and search

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.utils.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def minimalCreateRequest =
    """
    {
      'type': 'Project',
      'title': 'Test Project',
      'language': 'http://lexvo.org/id/iso639-3/nob',
      'startDate': '2010-06-29T01:33:17.518Z',
      'endDate': '2030-06-29T01:33:17.518Z',
      'coordinatingInstitution': {
        'type': 'Organization',
        'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
      },
      'contributors': [
        {
          'identity': {
            'type': 'Person',
            'id': 'https://api.cristin-test.uio.no/v2/persons/515114'
          },
          'roles': [
            {
              'type': 'ProjectManager',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
              }
            }
          ]
        }
      ]
    }
    """
    * def lessThanMinimalCreateRequest =
    """
    {
      'type': 'Project',
      'startDate': '2010-06-29T01:33:17.518Z',
      'endDate': '2030-06-29T01:33:17.518Z',
      'coordinatingInstitution': {
        'type': 'Organization',
        'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
      },
      'contributors': [
        {
          'identity': {
            'type': 'Person',
            'id': 'https://api.cristin-test.uio.no/v2/persons/515114'
          },
          'roles': [
            {
              'type': 'ProjectManager',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
              }
            }
          ]
        }
      ]
    }
    """
    * def requestWithIllegalValues =
    """
    {
      'type': 'Project',
      'title': 'Test Project',
      'startDate': '2010-06-29T01:33:17.518Z',
      'endDate': 'IllegalValue',
      'coordinatingInstitution': {
        'type': 'Organization',
        'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
      },
      'contributors': [
        {
          'identity': {
            'type': 'Person',
            'id': 'https://api.cristin-test.uio.no/v2/persons/515114'
          },
          'roles': [
            {
              'type': 'IllegalValue',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.cristin-test.uio.no/v2/institutions/20202'
              }
            }
          ]
        }
      ]
    }
    """
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE


  Scenario Outline: Create with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request minimalCreateRequest
    When method POST
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

  Scenario: Create returns status 401 Unauthorized when user not logged in
    Given path '/project'
    And request minimalCreateRequest
    When method POST
    Then status 401

  Scenario: Create returns status Bad request when input is insufficient
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request lessThanMinimalCreateRequest
    When method POST
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Supplied payload is not valid (title required)'

  Scenario: Create returns status Bad request when input has illegal values
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request requestWithIllegalValues
    When method POST
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
