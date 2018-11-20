Feature: Get instance or module files

  Background:
    Given an authenticated user

  Scenario: get files of an instance
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and an instance and valued properties
    When I get the instance files
    Then the files are successfully retrieved

  Scenario: get files of a deployed module
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and an instance and valued properties
    When I get the module files
    Then the files are successfully retrieved

  Scenario: get files of an instance that doesn't exist
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and valued properties
    When I try to get the instance files
    Then the resource is not found

  Scenario: get files of a deployed module that doesn't exist
    And an existing platform
    When I try to get the module files
    Then the resource is not found
