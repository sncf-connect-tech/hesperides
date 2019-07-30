Feature: Search modules

  Background:
    Given an authenticated user

  Scenario: search for an existing module
    Given a list of modules
    When I search for one specific module
    Then the module is found

  Scenario: search for an existing module using the wrong case
    Given a list of modules
    When I search for one specific module using the wrong case
    Then the module is found

  Scenario: search for existing modules
    Given a list of 12 modules
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

  Scenario: search for a limited number of existing modules
    Given a list of 12 modules
    When I search for some of those modules, limiting the number of results to 100
    Then the list of module results is limited to 12 items

  #issue-595
  Scenario: search for modules an there is an exact key match
    Given a list of 12 modules
    When I search for modules, using an existing module name, version and version type
    And a list of 10 elements is returned
    Then the first module in the results is this module

  #issue-595
  Scenario: search for modules an there is an exact name and version match
    Given a list of 10 modules with different names starting with the same prefix
    When I search for modules, using an existing module name and version
    And a list of 10 elements is returned
    Then the first module in the results has exactly this name and version

  #issue-595
  Scenario: search for modules an there is an exact name match
    Given a list of 10 modules with different names starting with the same prefix
    When I search for modules, using an existing module name
    And a list of 10 elements is returned
    Then the first module in the results has exactly this name
