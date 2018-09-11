Feature: Module information retrieval

  Regroup all uses cases related to the retrieval of module information.

  Background:
    Given an authenticated user

  Scenario: get all module names
    Given a list of 20 released modules
    When listing all modules names
    Then I get a distinct list of all modules names

  Scenario: get info for a given module
    Given an existing module
    When retrieving the module's info
    Then the module's info is retrieved

  Scenario: get info for a given released module
    Given a module that is released
    When retrieving the module's info
    Then the module's info is retrieved

  Scenario: get all versions for a given module
    Given an existing module with multiple versions
    When retrieving the module's versions
    Then the module's versions are retrieved

  Scenario: get all types for a given module version
    Given a module that is released
    When retrieving the module's types
    Then the module's types are workingcopy and release
