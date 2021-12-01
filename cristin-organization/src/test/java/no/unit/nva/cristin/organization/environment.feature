Feature: API tests for Cristin Organization retrieve and search

  Background:
    * def SERVER_URL = 'https://api.dev.nva.aws.unit.no'
    * def CRISTIN_BASE = SERVER_URL + '/cristin-karate-np3360'
#    Given url CRISTIN_BASE

  Scenario: Print environment
#    Then print 'url is : ', url
#    * print 'SERVER_URL is : ', SERVER_URL
    * print 'karate.properties[java.home]=', karate.properties['java.home']
#    * print myVarName
    * def environmentBaseUrl = karate.properties['baseUrl']
    * print environmentBaseUrl
    * print url

