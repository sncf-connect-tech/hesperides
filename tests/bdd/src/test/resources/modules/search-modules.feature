Feature: Search modules

  Background:
    Given an authenticated user

  Scenario: search for an existing module
    Given a list of modules
    When I search for one specific module
    Then the module is found

  Scenario: search for existing modules
    Given a list of modules
    When I search for some of those modules
    Then the list of module results is limited to 10 items

  Scenario: search for a module that does not exist
    Given a list of modules
    When I search for a module that does not exist
    Then an empty list is returned

  Scenario: search for a module without search terms
    Given a list of modules
    When I try to search for a module with no search terms
    Then the search request is rejected with a bad request error

  Scenario: search a single module that has been released without specifying the version type
    Given an existing module
    And I release this module
    When I search for a single module using only the name and version of this module
    Then I get the working copy version of this module

  Scenario: search a released single module that has been released
    Given an existing module
    And I release this module
    When I search for the released version of this single module
    Then I get the released version of this module

  Scenario: search a working copy single module that has been released
    Given an existing module
    And I release this module
    When I search for the working copy version of this single module
    Then I get the working copy version of this module
