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
      '@context': 'https://bibsysdev.github.io/src/project-context.json',
      'type': 'Project',
      'title': 'Example Title',
      'language': 'http://lexvo.org/id/iso639-3/nno',
      'alternativeTitles': [
        {
          'nb': 'Eksempel på tittel'
        }
      ],
      'startDate': '2012-01-09T00:00:00.001Z',
      'endDate': '2017-12-31T00:00:00.001Z',
      'funding': [
        {
          'type': 'Funding',
          'source': {
            'type': 'FundingSource',
            'names': {
              'nb': 'Egen institusjon'
            },
            'code': 'EI'
          },
          'code': '654321'
        }
      ],
      'coordinatingInstitution': {
        'type': 'Organization',
        'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/215.0.0.0'
      },
      'contributors': [
        {
          'type': 'ProjectManager',
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/325953'
          },
          'affiliation': {
            'type': 'Organization',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/215.0.0.0'
          }
        },
        {
          'type': 'ProjectParticipant',
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/326035'
          },
          'affiliation': {
            'type': 'Organization',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/215.0.0.0'
          }
        }
      ],
      'status': 'CONCLUDED',
      'academicSummary': {
        'en': 'Summary in an academic manner'
      },
      'popularScientificSummary': {
        'en': 'popularScientificSummary popularScientificSummary popularScientificSummary'
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
