Feature: API tests for Cristin Project Update

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def swaggerSampleProject =
    """
    {
      'title': 'Updated Title',
      'language': 'http://lexvo.org/id/iso639-3/eng',
      'startDate': '2022-10-09T00:00:00.001Z',
      'endDate': '2030-12-31T00:00:00.001Z',
      'coordinatingInstitution': {
        'type': 'Organization',
        'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0'
      },
      'contributors': [
        {
          'type': 'ProjectManager',
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/1684652'
          }
        }
      ],
      'funding': [
        {
          'source': 'https://api.dev.nva.aws.unit.no/cristin/funding-sources/NFR',
          'identifier': '1234'
        }
      ],
      'keywords': [
        {
          'type': '5686'
        },
        {
          'type': '4245'
        }
      ],
      'projectCategories': [
        {
          'type': 'PHD'
        }
      ],
      'relatedProjects': [
        'https://api.dev.nva.aws.unit.no/cristin/project/2057063'
      ],
      'academicSummary': {
        'en': 'Some academic summary',
        'nn': null
      },
      'popularScientificSummary': {
        'en': 'Some popular scientific summary',
        'nn': null
      },
      'method': {
        'en': 'Some method',
        'nn': null
      },
      'equipment': {
        'en': 'Some equipment',
        'nn': null
      },
      'contactInfo': {
        'contactPerson': 'Navn Navnesen',
        'organization': 'Universitetet i Oslo',
        'email': 'navn.navnesen@uio.no',
        'phone': null
      },
      'institutionsResponsibleForResearch': [
        {
          'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0'
        }
      ],
      'alternativeTitles': [
        {
          'nb': 'Tittel på norsk'
        },
        {
          'nn': null
        }
      ],
      'webPage': 'https://www.change.org'
    }
    """
    Given url CRISTIN_BASE

  Scenario: Update returns status 204 No Content on successful update of project
    Given path '/project/2675049'
    * header Authorization = 'Bearer ' + token
    And request swaggerSampleProject
    When method PATCH
    Then status 204
