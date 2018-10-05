Feature: Get global properties usage

  Background:
    Given an authenticated user

  Scenario: get global properties used in a platform module and techno template
    Given an existing techno with global properties
    And an existing module with global properties and this techno
    And an existing platform with global properties and this module
    When I get this platform global properties usage
    Then the platform global properties usage is successfully retrieved

  Scenario: get global properties used as deployed module property value
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with global properties and this module
    And the deployed module properties are valued with the platform global properties
    When I get this platform global properties usage
    Then the platform global properties usage is successfully retrieved
