Feature: Platform retrieval.

  Background:
    Given an authenticated user

  Scenario: get a platform
    Given an existing platform
    When retrieving this platform
    Then the platform is successfully retrieved

  Scenario: retrieve platforms using a module
    Given an existing module
    And existing platforms containing this module
    When retrieving the platforms containing this module
    Then the platforms are successfully retrieved

  Scenario: search for an existing platform with an application name and a platform name
    Given a list of 25 platforms
    When searching for one of them giving an application name and a platform name
    Then the platform is found

  Scenario: search all platforms from an application
    Given a list of 25 platforms
    When asking for the platform list of an application
    Then platform list is established for the targeted application
