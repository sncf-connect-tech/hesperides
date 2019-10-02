Feature: Get modules versions

  Background:
    Given an authenticated user

  Scenario: get the list of versions of an existing module
    Given a module with 4 versions
    When I get the module versions
    Then a list of 4 elements is returned

  Scenario: get the list of versions of a module that doesn't exist
    Given a module that doesn't exist
    When I get the module versions
    Then a list of 0 elements is returned

  Scenario: get a single version of a module that has been released
    Given an existing released module
    When I get the module versions
    Then a list of 1 element is returned
