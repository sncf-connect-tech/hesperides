Feature: Update module templates

  Background:
    Given an authenticated user

  Scenario: update an existing template in a module
    Given an existing module with a template
    And a template to update
    When I update this module template
    Then the module template is successfully updated

  Scenario: update an existing template in a released module
    Given a released module with a template
    And a template to update
    When I try to update this module template
    Then the module template update is rejected with a method not allowed error

  Scenario: update a template that doesn't exist in a module
    Given an existing module
    And a template that doesn't exist in this module
    When I try to update this module template
    Then the module template update is rejected with a not found error

  Scenario: update the wrong version of a template
    Given an existing module with a template
    And a template with an outdated version
    When I try to update this module template
    Then the module template update is rejected with a conflict error

  Scenario: update a template of a module that doesn't exist
    Given a module that doesn't exist
    And a template to update
    When I try to update this module template
    Then the module template update is rejected with a not found error

    #TODO Différencier outdated version et ?