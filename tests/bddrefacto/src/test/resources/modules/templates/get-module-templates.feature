Feature: Get module templates

  Background:
    Given an authenticated user

  Scenario: get the list of templates of an existing module
    Given an existing module
    And multiple templates in this module
    When I get the list of templates of this module
    Then a list of all the templates of the module is returned

  Scenario: get the list of templates of a released module
    Given an existing module
    And multiple templates in this module
    And I release this module
    When I get the list of templates of this module
    Then a list of all the templates of the module is returned

  Scenario: get a template of a module
    Given an existing module with a template
    When I get this template in this module
    Then the module template is successfully returned

  Scenario: get a template that doesn't exist in a a module
    Given an existing module
    And a template that doesn't exist in this module
    When I try to get this template in this module
    Then the module template is not found

  Scenario: get the list of templates of a module that doesn't exist
    Given a module that doesn't exist
    When I get the list of templates of this module
    Then the module templates is empty

  Scenario: get a template of a module that doesn't exist
    Given a module that doesn't exist
    When I try to get this template in this module
    Then the template module is not found
