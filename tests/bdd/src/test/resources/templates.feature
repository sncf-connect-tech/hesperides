Feature: modules templates

  Regroup all uses cases releated to modules templates manipulations.

  Background:
    Given an authenticated user

  Scenario: add a new template to an existing module working copy
    Given an existing module working copy
    And a template to create
    When adding a new template
    Then the template is successfully created and the module contains the new template

  Scenario: update an existing template from a module working copy
    Given an existing module working copy
    And an existing template in this module
    When updating this template
    Then the template is successfully updated

  Scenario: delete a template from a module working copy
    Given an existing module working copy
    And an existing template in this module
    When deleting this template
    Then the template is successfully deleted

  Scenario: conflict while updating an existing template
    Given an existing module working copy
    And an existing template in this module
    And this template is being modified alongside
    When updating this template
    Then the template update is rejected

  Scenario: get a template bundled in a module for a version workingcopy
    Given an existing module working copy
    And an existing template in this module
    When retrieving this template
    Then the template is retrieved

  Scenario: get all templates bundled in a module of a version workingcopy
    Given an existing module working copy
    And multiple templates in this module
    When retrieving those templates
    Then the templates are retrieved

  Scenario: get template bundled in a module for a version release
    Given an existing module working copy
    And an existing template in this module
    And this module is being released
    When retrieving this template
    Then the template is retrieved

  Scenario: get all templates bundled in a module of a version release
    Given an existing module working copy
    And multiple templates in this module
    And this module is being released
    When retrieving those templates
    Then the templates are retrieved