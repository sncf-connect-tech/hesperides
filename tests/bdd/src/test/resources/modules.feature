Feature: modules related features

  Regroup all uses cases related to module manipulations.

  Background:
    Given an authenticated user

  Scenario: create a new module working copy
    Given a module to create
    When creating a new module
    Then the module is successfully created

  Scenario: update a module working copy
    Given an existing module
    When updating this module
    Then the module is successfully updated

  Scenario: delete a module working copy
    Given an existing module
    When deleting this module
    Then the module is successfully deleted

  Scenario: delete a module release
    Given an existing released module
    When deleting this module
    Then the module is successfully deleted

  Scenario: conflict while updating an existing module
    Given an existing module
    And this module is being modified alongside
    When updating this module
    Then the module update is rejected

  Scenario: create a copy of an existing module
    Given an existing module
    When creating a copy of this module
    Then the module is successfully duplicated

  Scenario: search for an existing module
    Given a list of existing modules
    When searching for one of them
    Then it is found

  Scenario: search for existing modules
    Given a list of existing modules
    When searching for some of them
    Then the number of results is limited

  Scenario: search for a module that doesn't exist
    Given a list of existing modules
    When searching for one that does not exist
    Then the result is empty

  Scenario: get all module names
    Given a list of existing modules
    And a list of existing modules released
    When listing all modules names
    Then I get a distinct list of all modules names

  Scenario: get info for a given module
    Given an existing module
    When retrieving the module's info
    Then the module's info is retrieved

  Scenario: get info for a given released module
    Given an existing released module
    When retrieving the module's info
    Then the module's info is retrieved

  Scenario: get all versions for a given module
    Given an existing module with multiple versions
    When retrieving the module's versions
    Then the module's versions are retrieved

  Scenario: get all types for a given module version
    Given an existing module working copy and its release
    When retrieving the module's types
    Then the module's types are retrieved

# Gérer les cas de modules releasés
# Améliorer les vérifications en incluant des technos ?