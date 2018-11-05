Feature: Release modules

  Background:
    Given an authenticated user

  Scenario: release an existing module
    Given an existing techno
    And an existing module with this techno
    When I release this module
    Then the module is successfully released

  Scenario: release an already released module with the same version
    Given an existing techno
    And a released module with this techno
    When I try to release this module
    Then the module release is rejected with a conflict error

  Scenario: release an existing module with a different version
    Given an existing techno
    And an existing module with this techno
    When I release this module in version "2.0.0"
    Then the module is successfully released in version "2.0.0"

  Scenario: release a module that doesn't exist
    Given a module that doesn't exist
    When I try to release this module
    Then the module release is rejected with a not found error

  Scenario: release a module without specifying its version
    Given an existing techno
    And an existing module with this techno
    When I try to release this module without specifying its version
    Then the module release is rejected with a bad request error
