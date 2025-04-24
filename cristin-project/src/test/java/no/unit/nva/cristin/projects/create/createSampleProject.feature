Feature: API tests for creating sample Cristin Projects

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
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/1684651'
          },
          'roles': [
            {
              'type': 'ProjectManager',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0'
              }
            },
            {
              'type': 'ProjectParticipant',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0'
              }
            }
          ]
        },
        {
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/1684652'
          },
          'roles': [
            {
              'type': 'ProjectParticipant',
              'affiliation': {
                'type': 'Organization',
                'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0'
              }
            }
          ]
        },
        {
          'identity': {
            'type': 'Person',
            'email': 'nameless@example.org',
            'phone': '12345678',
            'firstName':'name',
            'lastName':'less'
          },
          'roles': [
            {
              'type': 'ProjectParticipant'
            }
          ]
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
          'type': 'SomeRandomValue',
          'source': 'https://api.dev.nva.aws.unit.no/cristin/funding-sources/NFR',
          'identifier': '1234',
          'labels': {
            'en': 'Research Council of Norway (RCN)'
          }
        }
      ],
      'keywords': [
        {
          'type': '5686'
        },
        {
          'type': '4245',
          'label': {
            'en': 'Some label'
          }
        }
      ],
      'projectCategories': [
        {
          'type': 'PHD',
          'label': {
            'en': 'Some label'
          }
        }
      ],
      'relatedProjects': [
        'https://api.dev.nva.aws.unit.no/cristin/project/2057063'
      ],
      'contactInfo': {
        'type': 'ContactInfo',
        'contactPerson': 'Navn Navnesen',
        'organization': 'Universitetet i Oslo',
        'email': 'navn.navnesen@uio.no',
        'phone': '99223344'
      },
      'webPage': 'https://www.example.org'
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
    And match response.contactInfo.contactPerson == 'Navn Navnesen'
    And match response.contactInfo.organization == 'Universitetet i Oslo'
    And match response.contactInfo.email == 'navn.navnesen@uio.no'
    And match response.contactInfo.phone == '99223344'
    And match response.relatedProjects == '#[1]'
    And match response.projectCategories == '#[1]'
    And match response.keywords == '#[2]'
    And match response.externalSources == '#[1]'
    And match response.funding == '#[1]'
    And match response.contributors == '#[3]'
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
          'identity': {
            'type': 'Person',
            'id': 'https://api.dev.nva.aws.unit.no/cristin/person/1684651'
          },
          'roles': [
            {
              'type': 'ProjectManager'
            }
          ]
        }
      ]
    }
    """
    Given path '/project'
    * header Authorization = 'Bearer ' + token
    And request swaggerMinimumSampleProject
    When method POST
    Then status 201
    And print response
    And match response.publishable == true
