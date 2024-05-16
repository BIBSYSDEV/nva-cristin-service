Feature: API tests for Cristin Person Update

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def username = java.lang.System.getenv('ADMIN_TESTUSER_ID')
    * def password = java.lang.System.getenv('ADMIN_TESTUSER_PASSWORD')
    * def simple_user_name = java.lang.System.getenv('SIMPLE_TESTUSER_ID')
    * def simple_user_password = java.lang.System.getenv('SIMPLE_TESTUSER_PASSWORD')
    * def cognitoClientAppId = java.lang.System.getenv('COGNITO_CLIENT_APP_ID')
    * def cognitoUserpoolId = java.lang.System.getenv('COGNITO_USER_POOL_ID')
    * def tokenGenerator = Java.type('no.unit.nva.cognito.CognitoUtil')
    * def token = tokenGenerator.loginUser(username, password, cognitoClientAppId)
    * def simpleUserToken = tokenGenerator.loginUser(simple_user_name, simple_user_password, cognitoClientAppId)
    * def invalidToken = 'just-a-invalid-token-for-now'
    * def personIdentifier = '1684653'
    * def nviPersonIdentifier = '1684654'

    * def updateFieldsRequest =
    """
    {
      'firstName': 'Cristin Test',
      'lastName': 'Bruker',
      'preferredFirstName': null,
      'keywords': [
        {
          'type': '3548',
          'label': {
            'en': 'Decision support'
          }
        },
        {
          'type': '5156',
          'label': {
            'en': 'Risk management'
          }
        }
      ],
      'background': {
        'en': 'My history as a researcher'
      },
      'contactDetails': {
        'telephone': '11223344',
        'email': 'test@example.org',
        'webPage': 'www.example.org'
      },
      'place': {
        'nb': 'Min institusjon',
        'en': 'My institution'
      },
      'collaboration': {
        'nb': 'Mine samarbeidende institusjoner',
        'en': null
      },
      'countries': [
        {
          'type': 'SE'
        },
        {
          'type': 'NO'
        }
      ],
      'awards': [
        {
          'name': 'My first award',
          'year': '2014',
          'type': {
            'type': 'ResearchDissemination'
          },
          'distribution': {
            'type': 'National'
          },
          'affiliation': {
            'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/185.11.0.0'
          }
        }
      ]
    }
    """
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Update returns status 401 Unauthorized when invalid token
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + invalidToken
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 401 Unauthorized when missing token
    Given path '/person/' + personIdentifier
    When method PATCH
    Then status 401
    And match response.message == 'Unauthorized'

  Scenario: Update returns status 204 when valid payload
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    And request updateFieldsRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 204 when sending null orcid to erase
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def validRequest =
    """
    {
      'orcid': null
    }
    """
    And request validRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when invalid orcid
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'orcid': '1234'
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'ORCID is not valid'

  Scenario: Update returns status 400 when trying to erase non nullable name
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'firstName': null
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Field firstName can not be erased'

  Scenario: Update returns status 400 when trying to set reserved to unsupported value
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'reserved': 'Hello'
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'Reserved field can only be set to boolean value'

  Scenario: Update returns status 400 when no supported values present
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def invalidRequest =
    """
    {
      'invalidField': 'invalidValue'
    }
    """
    And request invalidRequest
    When method PATCH
    Then status 400
    And match response.detail == 'No supported fields in payload, not doing anything'

  Scenario: Update returns status 204 when sending valid employments for own organization
    # If employment(s) is at organization not allowed to update, should silently ignore them
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': [
        {
          'type': 'https://api.dev.nva.aws.unit.no/cristin/position#1087',
          'organization': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0',
          'startDate': '2008-01-01T00:00:00Z',
          'endDate': '2030-12-31T00:00:00Z',
          'fullTimeEquivalentPercentage': 100
        }
      ]
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 204 when removing all employments.
    # Employments not allowed to delete should be silently ignored
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': []
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when sending invalid employments payload
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def employmentRequest =
    """
    {
      'employments': [
        {
          'hello': 'world'
        }
      ]
    }
    """
    And request employmentRequest
    When method PATCH
    Then status 400

  Scenario: Update returns status 204 when sending valid nvi data for own organization
    Given path '/person/' + nviPersonIdentifier
    * header Authorization = 'Bearer ' + token
    * def nviRequest =
    """
    {
      'nvi': {
          'verifiedBy': {
              'id': 'https://api.dev.nva.aws.unit.no/cristin/person/854279'
          },
          'verifiedAt': {
              'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0'
          }
      }
    }
    """
    And request nviRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 204 erasing nvi data at own institution.
    Given path '/person/' + nviPersonIdentifier
    * header Authorization = 'Bearer ' + token
    * def nviRequest =
    """
    {
      'nvi': null
    }
    """
    And request nviRequest
    When method PATCH
    Then status 204

  Scenario: Update returns status 400 when sending invalid nvi payload
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def nviRequest =
    """
    {
      'nvi': {
          'verifiedBy': {
              'id': 'https://api.dev.nva.aws.unit.no/cristin/person/854279'
          },
          'verifiedAt': {
              'hello': 'world'
          }
      }
    }
    """
    And request nviRequest
    When method PATCH
    Then status 400

  Scenario: Update filters out nvi and returns status 400 indicating no supported fields when trying to verify nvi at another institution
    Given path '/person/' + personIdentifier
    * header Authorization = 'Bearer ' + token
    * def nviRequest =
    """
    {
      'nvi': {
          'verifiedBy': {
              'id': 'https://api.dev.nva.aws.unit.no/cristin/person/854279'
          },
          'verifiedAt': {
             'id': 'https://api.dev.nva.aws.unit.no/cristin/organization/185.90.0.0'
          }
      }
    }
    """
    And request nviRequest
    When method PATCH
    Then status 400
    And match response.detail == 'No supported fields in payload, not doing anything'
