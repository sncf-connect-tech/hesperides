Feature: Delete modules

  Background:
    Given an authenticated user

  Scenario: delete an existing module
    Given an existing module
    When I delete this module
    Then the module is successfully deleted

  Scenario: delete a released module
    Given a released module
    When I delete this module
    Then the module is successfully deleted

  Scenario: delete a module that doesn't exist
    Given a module that doesn't exist
    When I try to delete this module
    Then the module deletion is rejected with a not found error