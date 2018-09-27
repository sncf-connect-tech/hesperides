Feature: Create modules

  Background:
    Given an authenticated user

  Scenario: create a new module
    Given an existing techno
    And a module to create with this techno
    When I create this module
    Then the module is successfully created

  Scenario: create a module that already exists
    Given an existing module
    And a module to create with the same name and version
    When I try to create this module
    Then the module creation is rejected with a conflict error

  Scenario: create a module after it has been deleted
#    Given an existing module
#    When I delete this module
#    And I create this module
#    Then the module is successfully created

  Scenario: create a module with a techno that doesn't exist
    Given a techno that doesn't exist
    And a module to create with this techno
    When I try to create this module
    Then the module creation is rejected with a not found error