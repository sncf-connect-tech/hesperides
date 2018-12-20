Feature: Get platforms

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing platform
    Given an existing module
    Given an existing platform with this module
    When I get the platform detail
    Then the platform detail is successfully retrieved

  Scenario: get the detail of an existing platform at a specific time
    Given an existing module
    Given an existing platform with this module
    When I get the platform detail at a specific time in the past
    Then the platform detail is successfully retrieved

  Scenario: get a platform that doesn't exist
    Given a platform that doesn't exist
    When I try to get the platform detail
    Then the resource is not found

  Scenario: get a platform with multiple logical groups and multiple modules to check the module auto-incremented identifiers
    Given an existing module named "module-1"
    And an existing platform with this module in logical group "group-1"
    And I update this platform, adding this module in logical group "group-2"
    And an existing module named "module-2"
    And I update this platform, adding this module in logical group "group-1"
    And I update this platform, adding this module in logical group "group-2"
    When I get the platform detail
    Then the platform detail is successfully retrieved
