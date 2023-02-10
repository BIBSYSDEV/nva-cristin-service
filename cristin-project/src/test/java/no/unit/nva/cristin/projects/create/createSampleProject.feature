Feature: API tests for Cristin Project retrieve and search

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
      '@context': 'https://bibsysdev.github.io/src/project-context.json',
      'type': 'Project',
      'title': 'Example Title',
      'language': 'http://lexvo.org/id/iso639-3/nno',
      'alternativeTitles': [
        {
          'nb': 'Eksempel på tittel'
        }
      ],
      'startDate': '2012-01-09T00:00:00.000Z',
      'endDate': '2017-12-31T00:00:00.000Z',
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
        },
        {
          'type': 'ProjectParticipant',
          'identity': {
            'type': 'Person',
            'email': 'ola.borte.moen@example.org',
            'phone': '12345678',
            'firstName':'Ola',
            'lastName':'Borte'
          }
        }
      ],
      'academicSummary': {
        'en': 'Summary in an academic manner'
      },
      'popularScientificSummary': {
        'en': 'popularScientificSummary popularScientificSummary popularScientificSummary'
      },
      'method': {
        'en': 'My method'
      },
      'equipment': {
        'en': 'My equipment'
      },
      'institutionsResponsibleForResearch': [
        {
          'type': 'Organization',
          'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0'
        }
      ],
      'healthProjectData': {
        'type': 'Drugstudy',
        'clinicalTrialPhase': 'PhaseIII'
      },
      'externalSources': [
        {
          'type': 'ExternalSource',
          'identifier': '123456',
          'name': 'REK'
        }
      ],
      'approvals': [
        {
          'type' : 'Approval',
          'date' : '2017-04-26T00:00:00.000Z',
          'authority' : 'RegionalEthicalCommittees',
          'status' : 'Approved',
          'applicationCode' : 'EthicalApproval',
          'identifier' : '2017/800'
        }
      ],
      'funding': [
        {
          'type': 'Funding',
          'source': {
            'type': 'FundingSource',
            'code': 'NFR'
          },
          'code': '1234'
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
        'https://api.dev.nva.aws.unit.no/cristin/project/6721135'
      ],
      'contactInfo': {
        'type': 'ContactInfo',
        'contactPerson': 'Navn Navnesen',
        'institution': 'Universitetet i Oslo',
        'email': 'navn.navnesen@uio.no',
        'phone': '99223344'
      }
    }
    """
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Create returns status 201 Created with fields from input when input is sample project from swagger documentation
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request swaggerSampleProject
    When method POST
    Then status 201
    And match response.method == '#present'
    And match response.equipment == '#present'
    And match response.institutionsResponsibleForResearch[0].id == '#present'
    And match response.approvals[0].date == '#present'
    And match response.approvals[0].authority == 'RegionalEthicalCommittees'
    And match response.approvals[0].status == 'Approved'
    And match response.approvals[0].applicationCode == 'EthicalApproval'
    And match response.approvals[0].identifier == '2017/800'
    And match response.approvals[0].authorityName == '#present'
    And print response

  Scenario: Creating project with only minimum required data returns 201 Created
    * def swaggerMinimumSampleProject =
    """
    {
      'title': 'Example Title',
      'startDate': '2012-01-09T00:00:00.000Z',
      'endDate': '2030-01-09T00:00:00.000Z',
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
          }
        }
      ]
    }
    """
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request swaggerMinimumSampleProject
    When method POST
    Then status 201
