Feature: modules templates

  Regroup all uses cases releated to modules templates manipulations.

  Background:
    Given an authenticated user

  Scenario: add a new template to an existing module
    Given an existing module
    And a template to create
    When adding a new template
    Then the template is successfully created and the module contains the new template

  Scenario: trying to add the same template twice to an existing module
    Given an existing module
    And a template to create
    When adding this template twice
    Then the second one is rejected

  Scenario: update an existing template from a module
    Given an existing module
    And an existing template in this module
    When updating this template
    Then the template is successfully updated

  Scenario: delete a template from a module
    Given an existing module
    And an existing template in this module
    When deleting this template
    Then the template is successfully deleted

  Scenario: trying to update a template twice at the same time
    Given an existing module
    And an existing template in this module
    When updating this template
    And updating the same version of the template alongside
    Then the template update is rejected

  Scenario: get a template bundled in a module for a version workingcopy
    Given an existing module
    And an existing template in this module
    When retrieving this template
    Then the template is retrieved

  Scenario: get all templates bundled in a module of a version workingcopy
    Given an existing module
    And multiple templates in this module
    When retrieving those templates
    Then the templates are retrieved

  Scenario: get template bundled in a module for a version release
    Given an existing module
    And an existing template in this module
    And this module is being released
    When retrieving this template
    Then the template is retrieved

  Scenario: get all templates bundled in a module of a version release
    Given an existing module
    And multiple templates in this module
    And this module is being released
    When retrieving those templates
    Then the templates are retrieved