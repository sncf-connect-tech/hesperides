Feature: Search modules

  Background:
    Given an authenticated user

  Scenario: search for an existing module
    Given a list of 12 modules
    When I search for one specific module
    Then the module is found

  Scenario: search for existing modules
    Given a list of 42 modules
    When I search for some of those modules
    Then the list of module results is limited to 10 items

  Scenario: search for a module that does not exist
    Given a list of 12 modules
    When I search for a module that does not exist
    Then the list of module results is empty
