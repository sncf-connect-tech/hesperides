Feature: Get applications

  Background:
    Given an authenticated user

  Scenario: get an existing application
    Given an existing module
    And an existing platform with this module
    When I get the platform application
    Then the application is successfully retrieved

  Scenario: get an existing application without modules
    Given an existing module
    And an existing platform with this module
    When I get the platform application with parameter hide_platform set to true
    Then the application is successfully retrieved

  Scenario: get an application that doesn't exist
    Given a platform that doesn't exist
    When I try to get the platform application
    Then the resource is not found

  Scenario: get all applications
    Given an existing module
    And a list of applications with platforms and this module
    When I get the list of all applications
    Then all the applications are retrieved with their platforms and their modules
