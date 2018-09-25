Feature: Create module templates

  Background:
    Given an authenticated user

  Scenario: add a template to an existing module
    Given an existing module
    And a template to create
    When I add this template to the module
    Then the template is successfully added to the module

  Scenario: add a template to a released module
    Given a released module
    And a template to create
    When I try to add this template to the module
    Then the module template creation is rejected with a method not allowed error

  Scenario: add a template to a module that doesn't exist
    Given a module that doesn't exist
    And a template to create
    When I try to add this template to the module
    Then the module template creation is rejected with a not found error

  Scenario: add a template without a name to an existing module
    Given an existing module
    And a template to create without a name
    When I try to add this template to the module
    Then the module template creation is rejected with a bad request error

  Scenario: add a template without a filename to an existing module
    Given an existing module
    And a template to create without a filename
    When I try to add this template to the module
    Then the module template creation is rejected with a bad request error

  Scenario: add a template without a location to an existing module
    Given an existing module
    And a template to create without a location
    When I try to add this template to the module
    Then the module template creation is rejected with a bad request error

  Scenario: create a template after it has been deleted
    Given an existing module with a template
    When I delete this module template
    And I add this template to the module
    Then the template is successfully added to the module
