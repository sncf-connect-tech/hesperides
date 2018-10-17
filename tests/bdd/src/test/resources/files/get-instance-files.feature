Feature: Get instance files

  Background:
    Given an authenticated user

  Scenario: get instance files
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and an instance and valued properties
    When I get the instance files
    Then the instance files are successfully retrieved
    