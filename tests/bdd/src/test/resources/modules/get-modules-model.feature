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
    Given an existing module with nested iterable properties
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: get the model of a module that doesn't exist
    Given a module that doesn't exist
    When I try to get the model of this module
    Then the module model is not found

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
    Given an existing module with properties with the same name and comment, but different default values, in two templates
    When I get the model of this module
    Then the model of this module has 1 simple property
#    And the model of this module contains the property with the same name and comment
    And the model of this module contains the properties

  Scenario: get the model of a module with properties with the same name but different comments in two templates
    Given an existing module with properties with the same name but different comments in two templates
    When I get the model of this module
    Then the model of this module has 2 simple properties
    And the model of this module contains the properties

  Scenario: get the model of a module with a template with variables in filename and location
    Given a template to create with filename "{{filename}}.json" with location "/{{location}}"
    And an existing module with this template
    When I get the model of this module
    Then the model of this module contains the properties

  Scenario: the model of a module property aggregates the annotations of all its definitions
    Given a template named "a" with the following content
      """
      {{foo|@required}}
      """
    And an existing module with this template
    And a template named "b" with the following content
      """
      {{foo|@password}}
      {{foo|@default BAR}}
      """
    And I add this template to the module
    When I get the model of this module
    Then the model of this module has 1 simple property
    Then the model of property "foo" is a required password and has a default value of "BAR"

  Scenario: model of a template containing 2 properties with same name and comment but different mustache content
    Given a template with the following content
      """
    <logger level="{{logging.level|@comment "some comments" @default INFO}}">
    <logger level="{{logging.level|some comments @default INFO}}">
      """
    And an existing module with this template
    When I get the model of this module
    Then the model of this module has 1 simple property
    Then the model of property "logging.level" has a comment of "some comments" and a default value of "INFO"
