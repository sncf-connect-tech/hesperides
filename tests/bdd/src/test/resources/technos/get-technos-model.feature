Feature: Get techno model

  Background:
    Given an authenticated user

  Scenario: get the model of a techno with properties
    Given an existing techno with properties
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno with iterable properties
    Given an existing techno with iterable properties
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno with iterable-ception
    Given an existing techno with iterable-ception
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the model of this techno
    Then the techno model if not found

  Scenario: get the model of a techno with a deleted template
    Given an existing techno with properties
    And I delete this techno template
    When I get the model of this techno
    Then the model of this techno doesn't contain the properties

  Scenario: get the model of a techno with an updated template
    Given an existing techno with properties
    And the techno template properties are modified
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno with properties with the same name and comment, but different default values, in two templates
    Given an existing techno with properties with the same name and comment, but different default values, in two templates
    When I get the model of this techno
    Then the model of this techno contains the properties

  Scenario: get the model of a techno with properties with the same name but different comments in two templates
    Given an existing techno with properties with the same name but different comments in two templates
    When I get the model of this techno
    Then the model of this techno contains the properties
