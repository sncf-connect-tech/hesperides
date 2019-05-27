Feature: Get applications

  Background:
    Given an authenticated user

  Scenario: list applications
    Given an existing platform
    When I get the applications list
    Then the application list contains 1 entry

  Scenario: get an existing application
    Given an existing module
    And an existing platform with this module
    When I get the application details
    Then the application is successfully retrieved

  Scenario: get an existing application without modules
    Given an existing module
    And an existing platform with this module
    When I get the application details with parameter hide_platform set to true
    Then the application is successfully retrieved

  Scenario: get an application that doesn't exist
    Given a platform that doesn't exist
    When I try to get the application details
    Then the resource is not found