@done
Feature: Search platforms

  Background:
    Given an authenticated user

  Scenario: search for an existing platform
    Given a list of 12 applications prefixed by "app" with 4 platforms prefixed by "plf" in each application
    When I search for the platform "plf-3" in the application "app-6"
    Then the platform search result contains 1 entry
    And the platform "plf-3" is found

  Scenario: search for all platforms
    Given a list of 12 applications prefixed by "app" with 4 platforms prefixed by "plf" in each application
    When I search for the platform "plf" in the application "app"
    Then the platform search result contains 4 entries

  Scenario: search for platforms with only one letter
    Given a list of 12 applications prefixed by "app" with 4 platforms prefixed by "plf" in each application
    When I search for the platform "p" in the application "a"
    Then the platform search result contains 4 entries

  Scenario: search for a platform that doesn't exist
    Given a list of 12 applications prefixed by "app" with 4 platforms prefixed by "plf" in each application
    When I search for the platform "plf-12" in the application "app-1"
    Then the platform search result contains 0 entries

  Scenario: search for a platform without specifying the application
    Given a list of 12 applications prefixed by "app" with 4 platforms prefixed by "plf" in each application
    When I try to search for the platform "plf-4" in the application ""
    Then the platform search is rejected with a bad request error

  # ne devrait pas passer en fake_mongo
  @require-real-mongo
  Scenario: search for platforms is case-insensitive
    Given an application named AVG with a platform named PRD1
    When I search for the platform "prd1" in the application "avg"
    Then the platform search result contains 1 entry
