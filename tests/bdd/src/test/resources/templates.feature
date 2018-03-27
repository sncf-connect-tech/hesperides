Feature: modules templates

  Regroup all uses cases releated to modules templates manipulations.

  Background:
    Given an authenticated user
    And an existing module

  Scenario: add a new template to an existing module
    Given a template to create
    When adding a new template
    Then the module contains the new template

  Scenario: update a existing template from a module working copy
    Given an existing template in this module
    When updating this template
    Then the template is successfully updated

  Scenario: delete a template from a module working copy
    Given an existing template in this module
    When deleting this template
    Then the template is successfully deleted
