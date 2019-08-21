@done
Feature: Create platforms

  Background:
    Given an authenticated user

  Scenario: create a platform with initial instance properties
    Given an existing module
    And a platform to create with this module and an instance with properties
    When I create this platform
    Then the platform is successfully created

  Scenario: create a platform with a module associated to an empty path
    Given an existing module
    And a platform to create with this module with an empty path and an instance with properties
    When I create this platform
    Then the platform is successfully created and the deployed module has the following path "#"

  Scenario: create a faulty platform
    Given a platform to create named "oops space"
    When I try to create this platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"

  Scenario: create a platform that already exist
    Given an existing platform
    And a platform to create
    When I try to create this platform
    Then the platform creation fails with a conflict error

  @require-real-mongo
  Scenario: forbid creation of a platform with a same name but different letter case
    Given an existing platform named "DEV"
    And a platform to create named "dev"
    When I try to create this platform
    Then the platform creation fails with a conflict error

  Scenario: create a platform after it has been deleted
    Given an existing platform
    And I delete this platform
    When I create this platform
    Then the platform is successfully created

  Scenario: create a platform with a space character in its version
    Given a platform to create with version "1 1"
    When I create this platform
    Then the platform is successfully created

  Scenario: create a platform without setting isProductionPlatform
    Given a platform to create without setting production flag
    When I create this platform
    Then the platform is successfully created