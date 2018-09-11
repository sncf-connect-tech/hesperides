Feature: Module template retrieval

  Regroup all uses cases releated to the retrieval of module templates.

  Background:
    Given an authenticated user

  Scenario: get a template bundled in a module for a version workingcopy
    Given an existing module
    And an existing template in this module
    When retrieving this module template
    Then the module template is retrieved

  Scenario: get all templates bundled in a module of a version workingcopy
    Given an existing module
    And multiple templates in this module
    When retrieving those templates
    Then the templates are retrieved

  Scenario: get template bundled in a module for a version release
    Given a module that is released
    And an existing template in this module
    When retrieving this module template
    Then the module template is retrieved

  Scenario: get all templates bundled in a module of a version release
    Given a module that is released
    And multiple templates in this module
    When retrieving those templates
    Then the templates are retrieved

  Scenario: get a template with its name ending in .sh bundled in a module
    Given an existing module
    And an existing template with a name ending in dot sh in this module
    When retrieving this module template with a name ending in dot sh
    Then the module template with a name ending in dot sh is retrieved