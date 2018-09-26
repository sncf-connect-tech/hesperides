Feature: Get modules versions

  Background:
    Given an authenticated user

  Scenario: get the list of versions of an existing module
    Given a module with 4 versions
    When I get the module versions
    Then a list of 4 versions is returned

  Scenario: get the list of versions of a module that doesn't exist
    Given a module that doesn't exist
    When I get the module versions
    Then a list of 0 versions is returned