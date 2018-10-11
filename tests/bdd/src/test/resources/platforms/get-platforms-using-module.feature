Feature: Get platforms using module

  Background:
    Given an authenticated user

  Scenario: get platforms using module
    Given an existing module
    And an existing platform named "P1" with this module
    And an existing platform named "P2" with this module
    When I get the platforms using this module
    Then the platforms using this module are successfully retrieved
