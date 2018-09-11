Feature: Module deletion

  Regroup all uses cases related to the deletion of modules.

  Background:
    Given an authenticated user

  Scenario: delete a module
    Given an existing module
    When deleting this module
    Then the module is successfully deleted

  Scenario: delete a released module
    Given a module that is released
    When deleting this module
    Then the module is successfully deleted
