Feature: Module update

  Regroup all uses cases related to the update of modules.

  Background:
    Given an authenticated user

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

  Scenario: trying to update a module twice at the same time
    Given an existing module
    When updating this module
    And updating the same version of the module alongside
    Then the module update is rejected
