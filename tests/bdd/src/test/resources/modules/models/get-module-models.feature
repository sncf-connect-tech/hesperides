Feature: Module model retrieval

  Regroup all uses cases related to the retrieval of module information.

  Background:
    Given an authenticated user

  Scenario: get the model of a module
    Given an existing module
    And a template with properties in this module
    When retrieving the model of this module
    Then the model of this module contains all the properties

  Scenario: get the model of a module thas has a techno with a template containing properties
    Given an existing techno
    And a template in this techno that has properties
    And an existing module
    And the techno is attached to the module
    When retrieving the model of this module
    Then the model of this module contains all the properties of the techno

#    https://github.com/voyages-sncf-technologies/hesperides/issues/244
#  Scenario: get the model of a module with a techno after the techno is updated
#    Given an existing techno
#    And an existing module
#    And the techno is attached to the module
#    And a template in this techno that has properties
#    When retrieving the model of this module
#    Then the model of this module contains all the properties of the techno

  Scenario: get a model from a module with a template that has ambiguous properties
    Given an existing module
    And a template in this module that has properties with the same name but different attributes
    When retrieving the model of this module
    Then the model of this module contains all the properties with the same name from this template

  Scenario: get a model from a module with templates that have ambiguous properties
    Given an existing module
    And templates in this module that have properties with the same name but different attributes
    When retrieving the model of this module
    Then the model of this module contains all the properties with the same name from these templates

  Scenario: get a model from a module after updating its template
    Given an existing module
    And a template in this module containing properties that have been updated
    When retrieving the model of this module
    Then the model of this module contains the updated properties

  Scenario: get a model from a module after delete its template
    Given an existing module
    And a template in this module containing properties but that is being deleted
    When retrieving the model of this module
    Then the model of this module does not contain the properties of the deleted template

  Scenario: get the model of a module that has iterable properties
    Given an existing module
    And a template in this module that has iterable properties
    When retrieving the model of this module
    Then the model of this module contains all the iterable properties

  Scenario: get the model of a module with a techno's template updated
    Given an existing techno
    And a template in this techno that has properties
    And an existing module
    And the techno is attached to the module
    And the techno is updated with new properties
    When retrieving the model of this module
    Then the model of this module contains the new properties

  Scenario: get the model of a module with a techno's template deleted
    Given an existing techno
    And a template in this techno that has properties
    And an existing module
    And the techno is attached to the module
    And the techno's template is deleted
    When retrieving the model of this module
    Then the model of this module does not contain the properties of the deleted techno template

  Scenario: get the model of a module with a techno's template added
    Given an existing techno
    And a template in this techno that has properties
    And an existing module
    And the techno is attached to the module
    And a new template is added to this techno
    When retrieving the model of this module
    Then the model of this module contains the new properties

  Scenario: get the model of a module with a deleted
    Given an existing techno
    And a template in this techno that has properties
    And an existing module
    And the techno is attached to the module
    And the techno is deleted
    When retrieving the model of this module
    Then the model of this module does not contain the properties of the deleted techno
