Feature: Module template deletion

  Regroup all uses cases releated to the deletion of module templates.

  Background:
    Given an authenticated user

  Scenario: delete a template from a module
    Given an existing module
    And an existing template in this module
    When deleting this template
    Then the template is successfully deleted