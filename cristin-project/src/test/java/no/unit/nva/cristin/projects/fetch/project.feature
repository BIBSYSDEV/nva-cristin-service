Feature: API tests for Cristin Project retrieve and search

  Background:
    * def domainName = java.lang.System.getenv('DOMAIN_NAME')
    * def basePath = java.lang.System.getenv('BASE_PATH')
    * def CRISTIN_BASE =  'https://' + domainName +'/' + basePath
    * def illegalIdentifier = 'illegalIdentifier'
    * def nonExistingProjectId = '0'
    * def validIdentifier = '304134'
    * def projectIdRegex = 'https:\/\/[^\/]+\/[^\/]+\/project\/[0-9]+'
    * def PROBLEM_JSON_MEDIA_TYPE = 'application/problem+json'
    Given url CRISTIN_BASE
    * print 'Current base url: ' + CRISTIN_BASE

  Scenario: Fetch and receive CORS preflight response
    * configure headers =
  """
    {
      'Origin': 'http://localhost:3000',
      'Accept': 'application/ld+json',
      'Referer': 'Not sure what the value should be yet',
      'Connection': 'keep-alive',
      'Accept-Encoding': 'gzip, deflate, br',
      'Access-Control-Request-Method': 'GET',
      'Access-Control-Request-Headers': 'Content-Type, Authorization'
    }
  """
    Given path '/project/1234'
    When method OPTIONS
    Then status 200
    And match responseHeaders['Access-Control-Allow-Origin'][0] == '*'
    * def accessControlAllowMethods = responseHeaders['Access-Control-Allow-Methods'][0]
    And match accessControlAllowMethods contains 'GET'
    And match accessControlAllowMethods contains 'OPTIONS'
    * def accessControlAllowHeaders = responseHeaders['Access-Control-Allow-Headers'][0]
    And match accessControlAllowHeaders contains 'Content-Type'
    And match accessControlAllowHeaders contains 'X-Amz-Date'
    And match accessControlAllowHeaders contains 'Authorization'
    And match accessControlAllowHeaders contains 'X-Api-Key'
    And match accessControlAllowHeaders contains 'X-Amz-Security-Token'
    And match accessControlAllowHeaders contains 'Access-Control-Allow-Origin'

  Scenario Outline: Fetch returns valid data and with correct content negotiation <CONTENT_TYPE>
    * configure headers = { 'Accept': <CONTENT_TYPE> }
    Given path '/project/' + validIdentifier
    When method GET
    Then status 200
    And match response == '#object'
    And match response['@context'] == '#present'
    And match response.id == '#regex ' + projectIdRegex
    And match response.type == 'Project'

    Examples:
      | CONTENT_TYPE          |
      | 'application/ld+json' |
      | 'application/json'    |

  Scenario Outline: Fetch with unsupported Accept header returns Unsupported Media Type
    * configure headers = { 'Accept': <UNACCEPTABLE_CONTENT_TYPE> }
    Given path '/project/' + validIdentifier
    When method GET
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

  Scenario: Fetch returns status Bad request when requesting illegal project identifier
    Given path '/project/' + illegalIdentifier
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == 'Invalid path parameter for identifier, needs to be a number'

  Scenario: Fetch returns status Bad request when sending illegal query parameter
    Given path '/project/' + validIdentifier
    And param invalidParam = 'someValue'
    When method GET
    Then status 400
    And match response.title == 'Bad Request'
    And match response.status == 400
    And match response.detail == "This endpoint does not support query parameters"

  Scenario: Fetch returns status Not found when requesting unknown project identifier
    Given path '/project/' + nonExistingProjectId
    When method GET
    Then status 404
    And match response.title == 'Not Found'
    And match response.status == 404
    * def uri = 'https://api.dev.nva.aws.unit.no/' + basePath + '/project/0'
    And match response.detail == "The requested resource '" + uri + "' was not found"

  Scenario: Fetch returns supported fields
    Given path '/project/550767'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.published == true
    And match response.publishable == true
    And match response.created.date == '#present'
    And match response.lastModified.date == '#present'
    And match response.contactInfo.type == 'ContactInfo'
    And match response.contactInfo.contactPerson == '#present'
    And match response.contactInfo.organization == '#present'
    And match response.contactInfo.email == '#present'
    And match response.contactInfo.phone == '#present'
    And match response.fundingAmount.type == 'FundingAmount'
    And match response.fundingAmount.currency == '#present'
    And match response.fundingAmount.value == '#present'
    And match response.projectCategories[0].type == '#present'
    And match response.projectCategories[0].label == '#present'
    And match response.keywords[0].type == '#present'
    And match response.keywords[0].label == '#present'
    And match response.relatedProjects[0] == '#present'
    And match response.funding[0].source == '#present'
    And match response.funding[0].identifier == '#present'
    And match response.funding[0].labels == '#present'
    And match response.contributors[0].roles[0].type == '#present'
    And match response.creator.identity.id == '#present'

  Scenario: Fetch returns id on identified persons
    Given path '/project/2676613'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.contributors[0].identity.id == '#present'

  Scenario: Fetch returns method and equipment
    Given path '/project/284612'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.method['no'] == '#present'
    And match response.equipment['no'] == '#present'

  Scenario: Fetch returns project external sources
    Given path '/project/529587'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.externalSources[0].type == 'ExternalSource'
    And match response.externalSources[0].identifier == '#present'
    And match response.externalSources[0].name == '#present'

  Scenario: Fetch returns project institutions responsible for research
    Given path '/project/548126'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.institutionsResponsibleForResearch[0].id == '#present'
    And match response.institutionsResponsibleForResearch[0].labels == '#present'

  Scenario: Fetch returns project with additional health data and approvals
    Given path '/project/538881'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.healthProjectData == '#present'
    And match response.healthProjectData.type == 'Drugstudy'
    And match response.healthProjectData.label == '#present'
    And match response.healthProjectData.clinicalTrialPhase == 'PhaseIII'
    And match response.approvals[0].authority == 'RegionalEthicalCommittees'
    And match response.approvals[0].status == 'Approved'
    And match response.approvals[0].applicationCode == 'EthicalApproval'
    And match response.approvals[0].identifier == '2017/800'
    And match response.approvals[0].authorityName == '#present'

  Scenario: Fetch returns project with webpage
    Given path '/project/2675101'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.webPage == '#present'
    And match response.webPage == 'https://www.example.org'

  Scenario: Fetch returns project without webpage when not present in upstream
    Given path '/project/550767'
    When method GET
    Then status 200
    And match response == '#object'
    And match response.webPage != '#present'
