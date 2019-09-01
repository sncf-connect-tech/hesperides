Feature: Get platform at a given point in time

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing platform at a specific time in the past
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I update this platform, removing this module
    And I get the platform detail at a specific time in the past
    Then the initial platform detail is successfully retrieved

  Scenario: get the detail of an existing platform at a time where it did not exist
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I try to get the platform detail at the time of the EPOCH
    Then the resource is not found

  Scenario: get properties of a platform with valued properties at a specific time in the past
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I update the properties
    And I get the platform properties for this module at a specific time in the past
    Then the initial platform properties are successfully retrieved
