Feature: Module retrieval

  Regroup all uses cases related to the retrieval of modules.

  Background:
    Given an authenticated user

  Scenario: search for an existing module
    Given a list of 20 modules
    When searching for one of them
    Then it is found

  Scenario: search for existing modules
    Given a list of 20 modules
    When searching for some of them
    Then the number of module results is 10

  Scenario: search for a module that doesn't exist
    Given a list of 20 modules
    When searching for one that does not exist
    Then the number of module results is 0
