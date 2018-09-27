Feature: Release modules

  Background:
    Given an authenticated user

  Scenario: release an existing module
    Given an existing techno
    And an existing module with this techno
    When I release this module
    Then the module is successfully released

  Scenario: release a module that doesn't exist
    Given a module that doesn't exist
    When I try to release this module
    Then the module release is rejected with a not found error
