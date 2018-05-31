Feature: modules related features

  Regroup all uses cases related to module manipulations.

  Background:
    Given an authenticated user

  Scenario: create a new module
    Given a module to create
    When creating a new module
    Then the module is successfully created

  Scenario: update a module
    Given an existing module
    When updating this module
    Then the module is successfully updated

  Scenario: update a module containing a template
    Given an existing module
    And an existing template in this module
    When updating this module
    Then the module is successfully updated
    And the module contains the template

  Scenario: delete a module
    Given an existing module
    When deleting this module
    Then the module is successfully deleted

  Scenario: delete a released module
    Given a module that is released
    When deleting this module
    Then the module is successfully deleted

  Scenario: trying to update a module twice at the same time
    Given an existing module
    When updating this module
    And updating the same version of the module alongside
    Then the module update is rejected

  Scenario: create a copy of an existing module
    Given an existing techno
    And an existing module
    And the techno is attached to the module
    And an existing template in this module
    When creating a copy of this module
    Then the module is successfully and completely duplicated

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

  Scenario: create a release from an existing workingcopy
    Given an existing module
    When releasing the module
    Then the module is released