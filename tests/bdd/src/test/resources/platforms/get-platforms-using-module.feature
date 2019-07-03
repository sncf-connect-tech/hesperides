Feature: Get platforms using module

  Background:
    Given an authenticated user

  Scenario: get platforms using module
    Given an existing module
    And an existing platform named "P1" with this module
    And an existing platform named "P2" with this module
    When I get the platforms using this module
    Then the platforms using this module are successfully retrieved

  #issue-685
  Scenario: get platforms using module
    Given an existing module
    And an existing platform named "P1" with this module
    And an existing platform named "P2" with two modules : one with the same name and one with the same version
    When I get the platforms using this module
    Then a single platform is retrieved
