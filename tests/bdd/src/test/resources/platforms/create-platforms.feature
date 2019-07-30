Feature: Create platform

  Background:
    Given an authenticated user

  Scenario: create a platform with initial instance properties
    Given an existing module
    And a platform to create with this module with an instance with properties
    When I create this platform
    Then the platform is successfully created

  Scenario: create a platform with a module associated to an empty path
    Given an existing module
    And a platform to create with this module associated to an empty path with an instance with properties
    When I create this platform
    Then the platform is successfully created with "#" as path

  Scenario: create a faulty platform
    Given a platform to create, named "oops space"
    When I try to create this platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"

  Scenario: create a platform that already exist
    Given an existing platform
    And a platform to create
    When I try to create this platform
    Then the platform creation fails with an already exist error

  @require-real-mongo
  Scenario: forbid creation of a platform with a same name but different letter case
    Given an existing platform
    And a platform to create with the same name but different letter case
    When I try to create this platform
    Then the platform creation fails with an already exist error

  Scenario: create a platform after it has been deleted
    Given an existing platform
    When I delete this platform
    And I create this platform
    Then the platform is successfully created

  Scenario: create a platform with a space character in its version
    Given a platform to create with version "a b"
    When I create this platform
    Then the platform is successfully created

  Scenario: create a platform without setting isProductionPlatform
    Given a platform to create without setting isProductionPlatform
    When I create this platform
    Then the platform is successfully created