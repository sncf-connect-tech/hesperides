Feature: Copy modules

  Background:
    Given an authenticated user

  Scenario: copy an existing module
    Given an existing techno with properties
    And an existing module with properties and this techno
    When I create a copy of this module
    Then the module is successfully duplicated
    And the model of the duplicated module is the same

  Scenario: copy a released module
    Given an existing released techno with properties
    And an existing released module with properties and this techno
    When I create a copy of this module
    Then the module is successfully duplicated
    And the model of the duplicated module is the same

  Scenario: copy a module without specifying the version of the source module
    Given an existing techno with properties
    And an existing module with properties and this techno
    When I try to create a copy of this module without specifying the version of the source module
    Then the module copy is rejected with a bad request error

  Scenario: copy a module without specifying if source module is a working copy
    Given an existing released techno with properties
    And an existing released module with properties and this techno
    When I try to create a copy of this module without specifying whether it is a workingcopy
    Then the module copy is rejected with a bad request error

  Scenario: copy a module that doesn't exist
    Given a module that doesn't exist
    When I try to create a copy of this module
    Then the module copy is rejected with a not found error

  Scenario: copy a module using the same key
    Given an existing module
    When I try to create a copy of this module, using the same key
    Then the module copy is rejected with a conflict error
