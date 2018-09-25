Feature: Get module model

  Background:
    Given an authenticated user

  Scenario: get the model of a module with properties
    Given an existing module with properties
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module with iterable properties
    Given an existing module with iterable properties
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module with iterable-ception
    Given an existing module with iterable-ception
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module that doesn't exist
    Given a module that doesn't exist
    When I try to get the model of this module
    Then the module model if not found

  Scenario: get the model of a module with a deleted template
    Given an existing module with properties
    And I delete this module template
    When I get the model of this module
    Then the model of this module doesn't contain the properties

  Scenario: get the model of a module with an updated template
    Given an existing module with properties
    And the module template properties are modified
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module with properties with the same name and comment, but different default values, in two templates
    Given an existing module with properties with the same name and comment, but different default values, in two templates
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module with properties with the same name but different comments in two templates
    Given an existing module with properties with the same name but different comments in two templates
    When I get the model of this module
    Then the model of this module contains the properties
