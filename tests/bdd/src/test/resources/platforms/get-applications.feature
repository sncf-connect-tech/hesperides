Feature: Get applications

  Background:
    Given an authenticated user

  Scenario: list applications
    Given an existing platform
    When I get the applications name
    Then the application list contains 1 entry

  Scenario: get an existing application
    Given an existing module
    And an existing platform with this module
    When I get the application detail
    Then the application is successfully retrieved

  Scenario: get an existing application without modules
    Given an existing module
    And an existing platform with this module
    When I get the application detail with parameter hide_platform set to true
    Then the application is successfully retrieved

  Scenario: get an application that doesn't exist
    Given a platform that doesn't exist
    When I try to get the application detail
    Then the resource is not found

  Scenario: get all applications
    Given an existing module
    And a list of applications with platforms and this module
    When I get all the applications detail
    Then all the applications are retrieved with their platforms and their modules
