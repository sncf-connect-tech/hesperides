Feature: Get modules type

  Background:
    Given an authenticated user

  Scenario: get the types of a released module
    Given a released module
    When I get the module types
    Then a list containing workingcopy and release is returned

  Scenario: get the types of a module that is not released
    Given an existing module
    When I get the module types
    Then a list containing workingcopy is returned

  Scenario: get the types of a module that doesn't exist
    Given a module that doesn't exist
    When I get the module types
    Then a list containing nothing is returned