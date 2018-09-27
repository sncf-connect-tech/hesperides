Feature: Copy modules

  Background:
    Given an authenticated user

  Scenario: copy an existing module
    Given an existing techno with properties
    And an existing module with properties and this techno
    When I create a copy of this module
    Then the module is successfully duplicated
    And the model of the module is the same

  Scenario: copy a released module
    Given an existing techno with properties
    Given a released module with properties and this techno
    When I create a copy of this module
    Then the module is successfully duplicated
    But the version type of the duplicated module is working copy
    And the model of the module is the same

  Scenario: copy a module that doesn't exist
    Given a module that doesn't exist
    When I try to create a copy of this module
    Then the module copy is rejected with a not found error

  Scenario: copy a module using the same key
    Given an existing module
    When I try to create a copy of this module, using the same key
    Then the module copy is rejected with a conflict error
