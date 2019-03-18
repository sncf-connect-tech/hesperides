Feature: Get module model

  Background:
    Given an authenticated user

  Scenario: get the model of a module with properties
    Given an existing techno with properties
    And an existing module with properties and this techno
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
    Then the model of this module contains the updated properties

  Scenario: get the model of a module with properties with the same name and comment but different default values in multiple templates
    Given an existing module with properties with the same name and comment but different default values in multiple templates
    When I get the model of this module
    Then the model of this module lists 1 property
    And the model of this module contains the property with the same name and comment

  Scenario: get the model of a module with properties with the same name but different comments in two templates
    Given an existing module with properties with the same name but different comments in two templates
    When I get the model of this module
    Then the model of this module lists 2 properties
    And the model of this module contains the properties

  Scenario: get the model of a module with a template with variables in filename and location
    Given a template to create with filename "{{filename}}.json" with location "/{{location}}"
    And an existing module with this template
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: the model of a module property aggregates the annotations of all its definitions
    Given an existing module with this template content
      """
      {{foo|@required}}
      """
    And another template in this module with this content
      """
      {{foo|@password}}
      {{foo|@default BAR}}
      """
    When I get the model of this module
    Then the model of this module lists 1 property
    Then the model of property "foo" is a required password and has a default value of "BAR"

  Scenario: model of a template containing 2 properties with same name and comment but different mustache content
    Given an existing module with this template content
      """
    <logger level="{{logging.level|@comment "some comments" @default INFO}}">
    <logger level="{{logging.level|some comments @default INFO}}">
      """
    When I get the model of this module
    Then the model of this module lists 1 property
    Then the model of property "logging.level" has a comment of "some comments" and a default value of "INFO"
