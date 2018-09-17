Feature: Module creation

  Regroup all uses cases related to the creation of modules.

  Background:
    Given an authenticated user

  Scenario: create a new module
    Given a module to create
    When creating a new module
    Then the module is successfully created

  Scenario: create a module and a techno that have the same key
    Given an existing techno
    When creating a module that has the same key as this techno
    Then the module is successfully created

#  Scenario: create a copy of an existing module
#    Given an existing module
#    And a template with properties in this module
#    And an existing techno for this module
#    And a template with properties in this techno
##    And the techno is attached to the module
##    And an existing template in this module
#    When I create a copy of this module
#    Then the module is successfully and completely duplicated
#    And the model of the module is also duplicated

#  Scenario: create a copy of an existing module
#    Given a techno template with properties
#    And a techno with this information
#    And a module template with properties
#    And a module with this information
#    When I create a copy of this module
#    Then the module is duplicated with the technos of the original
#    And the model of the module is also duplicated

  Scenario: create a release from an existing workingcopy
    Given an existing module
    When releasing the module
    Then the module is released
