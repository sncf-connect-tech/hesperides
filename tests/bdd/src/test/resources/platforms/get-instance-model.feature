Feature: Get instance model

  Background:
    Given an authenticated user

  Scenario: get a platform module instance model
    Given an existing module with properties
    And an existing platform with this module and an instance and instance properties
    When I get the instance model
    Then the instance model is successfully retrieved
