Feature: Create platform

  Background:
    Given an authenticated user

  Scenario: create a platform with initial instance properties
    Given an existing module
    And a platform to create with this module with an instance with properties
    When I create this platform
    Then the platform is successfully created

  Scenario: create a faulty platform
    Given a platform to create, named "oops space"
    When I try to create this platform
    Then a 400 error is returned, blaming "platform_name contains an invalid character"

  Scenario: create a platform that already exist
    Given an existing platform
    And a platform to create
    When I try to create this platform
    Then the platform creation fails with an already exist error

  @integ-test-only
  Scenario: forbid creation of a platform with a same name but different letter case
    Given an existing platform
    And a platform to create with the same name but different letter case
    When I try to create this platform
    Then the platform creation fails with an already exist error

  Scenario: copy of a platform with its modules and properties
    Given an existing techno with properties and global properties
    And an existing module with properties and global properties and this techno
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I copy this platform
    Then the platform is successfully created
    And the platform property values are also copied

  Scenario: copy a platform that doesn't exist
    Given a platform that doesn't exist
    When I try to copy this platform
    Then the platform copy fails with a not found error

  Scenario: copy a platform using a key that already exist
    Given an existing platform
    When I try to copy this platform using the same key
    Then the platform copy fails with an already exist error

  Scenario: create a platform after it has been deleted
    Given an existing platform
    When I delete this platform
    And I create this platform
    Then the platform is successfully created