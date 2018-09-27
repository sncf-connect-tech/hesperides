Feature: Delete module templates

  Background:
    Given an authenticated user

  Scenario: delete an existing template in a module
    Given an existing module with a template
    And a template in this module
    When I delete this module template
    Then the module template is successfully deleted

  Scenario: delete an existing template in a released module
    Given a released module with a template
    When I try to delete this module template
    Then the module template delete is rejected with a method not allowed error

  Scenario: delete a template that doesn't exist in a module
    Given an existing module
    And a template that doesn't exist in this module
    When I try to delete this module template
    Then the module template delete is rejected with a not found error

  Scenario: delete a template of a module that doesn't exist
    Given a module that doesn't exist
    When I try to delete this module template
    Then the module template delete is rejected with a not found error
