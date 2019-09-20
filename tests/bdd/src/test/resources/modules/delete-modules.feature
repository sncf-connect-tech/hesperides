Feature: Delete modules

  Background:
    Given an authenticated user

  Scenario: delete an existing module
    Given an existing module
    When I delete this module
    Then the module is successfully deleted

  Scenario: delete a released module
    Given an existing released module
    When I delete this module
    Then the module is successfully deleted

  Scenario: delete a module that doesn't exist
    Given a module that doesn't exist
    When I try to delete this module
    Then the module deletion is rejected with a not found error

  Scenario: delete an existing module used by a plateforme
    Given an existing module
    And an existing platform with this module
    When I try to delete this module
    Then the module deletion is rejected with a conflict error

  Scenario: delete an existing module used by a deleted plateforme
    Given an existing module
    And an existing platform with this module
    When I try to delete this platform
    And the platform is successfully deleted
    And I try to delete this module
    Then the module is successfully deleted
