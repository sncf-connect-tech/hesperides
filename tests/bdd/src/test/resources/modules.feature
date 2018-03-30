Feature: modules related features

  Regroup all uses cases releated to module manipulations.

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

  Scenario: conflict while updating an existing module
    Given an existing module
    And this module is being modified alongside
    When updating this module
    Then the module update is rejected

#  Scenario: create a copy of an existing module
#    Given an existing module
#    When creating a copy of this module
#    Then the module is successfully created