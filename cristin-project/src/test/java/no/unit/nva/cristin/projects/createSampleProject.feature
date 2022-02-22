Feature: API tests for Cristin Project retrieve and search

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('TESTUSER_FEIDE_ID')
    * def password = java.lang.System.getenv('TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoUserpoolId, cognitoClientAppId)
    * def swaggerSampleProject =
    """
    {
      'title' : {
        'nn' : 'Example Title',
        'nb' : 'Eksempel på tittel'
      },
      'main_language' : 'nn',
      'start_date' : '2012-01-09T00:00:00.001Z',
      'end_date' : '2017-12-31T00:00:00.001Z',
      'status' : 'CONCLUDED',
      'created' : { },
      'last_modified' : { },
      'coordinating_institution' : {
        'unit' : {
          'cristin_unit_id' : '215.0.0.0'
        }
      },
      'project_funding_sources' : [ {
        'funding_source_code' : 'EI',
        'project_code' : '654321',
        'funding_source_name' : {
          'nb' : 'Egen institusjon'
        }
      } ],
      'participants' : [ {
        'cristin_person_id' : '325953',
        'url' : 'https://api.cristin-test.uio.no/v2/persons/325953',
        'roles' : [ {
          'role_code' : 'PRO_MANAGER',
          'unit' : {
            'cristin_unit_id' : '215.0.0.0'
          }
        } ]
      }, {
        'cristin_person_id' : '326035',
        'url' : 'https://api.cristin-test.uio.no/v2/persons/326035',
        'roles' : [ {
          'role_code' : 'PRO_PARTICIPANT',
          'unit' : {
            'cristin_unit_id' : '215.0.0.0'
          }
        } ]
      } ],
      'academic_summary' : {
        'en' : 'Summary in an academic manner'
      },
      'popular_scientific_summary' : {
        'en' : 'popularScientificSummary popularScientificSummary popularScientificSummary'
      }
    }
    """
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Create returns status 201 Created when input is sample project from swagger documentation
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request swaggerSampleProject
    When method POST
    Then status 201
    And print response
