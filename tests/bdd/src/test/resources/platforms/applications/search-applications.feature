Feature: Search applications

  Background:
    Given an authenticated user

  Scenario: search for an existing application
    Given a list of 12 applications prefixed by "app"
    When I search for the application "app-6"
    Then the application search result contains 1 entry
    And the application "app-6" is found

  Scenario: search for multiple applications
    Given a list of 12 applications prefixed by "app"
    When I search for the application "app"
    Then the application search result contains 12 entries

  Scenario: search for multiple applications with only one letter
    Given a list of 12 applications prefixed by "app"
    When I search for the application "a"
    Then the application search result contains 12 entries

  Scenario: search for an application that doesn't exist
    Given a list of 12 applications prefixed by "app"
    When I search for the application "app-13"
    Then the application search result contains 0 entries

  Scenario: search for an application without specifying the name
    Given a list of 12 applications prefixed by "app"
    When I search for the application ""
    Then the application search result contains 12 entries

  @require-real-mongo
  Scenario: search for applications is case-insensitive
    Given an application named AVG
    When I search for the application "avg"
    Then the application search result contains 1 entry
