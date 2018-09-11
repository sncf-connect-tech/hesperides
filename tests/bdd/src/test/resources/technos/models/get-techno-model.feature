Feature: Techno models retrieval

  Regroup all use cases related to the retrieval of techno models

  Background:
    Given an authenticated user

  Scenario: get the model of a techno
    Given an existing techno
    And a template in this techno that has properties
    When retrieving the model of this techno
    Then the model of this techno contains all the properties

  Scenario: get a model from a techno with a template that has ambiguous properties
    Given an existing techno
    And a template in this techno that has properties with the same name but different attributes
    When retrieving the model of this techno
    Then the model of this techno contains all the properties with the same name from this template

  Scenario: get a model from a techno with templates that have ambiguous properties
    Given an existing techno
    And templates in this techno that have properties with the same name but different attributes
    When retrieving the model of this techno
    Then the model of this techno contains all the properties with the same name from these templates

  Scenario: get a model from a techno after updating its template
    Given an existing techno
    And a template in this techno containing properties that have been updated
    When retrieving the model of this techno
    Then the model of this techno contains the updated properties

  Scenario: get a model from a techno after delete its template
    Given an existing techno
    And a template in this techno containing properties but that is being deleted
    When retrieving the model of this techno
    Then the model of this techno does not contain the properties of the deleted template

  Scenario: get the model of a techno that has iterable properties
    Given an existing techno
    And a template in this techno that has iterable properties
    When retrieving the model of this techno
    Then the model of this techno contains all the iterable properties
