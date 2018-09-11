Feature: Module template creations

  Regroup all uses cases releated to the creation of module templates.

  Background:
    Given an authenticated user

  Scenario: add a new template to an existing module
    Given an existing module
    When adding a new template to this module
    Then the template is successfully created and the module contains the new template

  Scenario: trying to add the same template twice to an existing module
    Given an existing module
    When adding a new template to this module
    And trying to add the same template to this module
    Then the second attempt to add the template to the module is rejected