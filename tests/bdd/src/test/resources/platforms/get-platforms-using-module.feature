Feature: Get platforms using module

  Background:
    Given an authenticated user

  Scenario: get platforms using the same module
    Given an existing module
    And an existing platform named "P1" with this module
    And an existing platform named "P2" with this module
    When I get the platforms using this module
    Then the platforms using this module are successfully retrieved

  #issue-685
  Scenario: get platforms using module with modules with the same name or the same version
    Given an existing module named "module-a" with version "1"
    And an existing platform named "P1" with this module
    And an existing module named "module-a" with version "2"
    And an existing platform named "P2" with this module
    And an existing module named "module-b" with version "1"
    And an existing platform named "P3" with this module
    When I get the platforms using this module
    Then a single platform is retrieved
