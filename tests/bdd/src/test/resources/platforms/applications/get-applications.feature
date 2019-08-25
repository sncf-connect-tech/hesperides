@done
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
    When I get the application detail without the platform modules
    Then the application is successfully retrieved without the platform modules

  Scenario: get an application that doesn't exist
    Given a platform that doesn't exist
    When I try to get the application detail
    Then the resource is not found

  Scenario: get all applications
    Given an existing module
    And a list of applications with platforms and this module
    When I get all the applications detail
    Then all the applications are retrieved with their platforms and their modules

  Scenario: retrieve the password flag for all platforms of an application
    Given an existing module with a template and properties and password properties
    And an existing platform with this module
    When I get the application detail requesting the password flag
    Then the application platform has the password flag

  Scenario: retrieve the password flag for all platforms of all applications
    Given an existing module with a template and properties and password properties
    And an existing platform with this module
    When I get all the applications detail requesting the password flag
    Then the applications platforms have the password flag