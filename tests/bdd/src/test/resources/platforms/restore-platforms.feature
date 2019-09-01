Feature: Restore platforms

  Background:
    Given an authenticated user

  Scenario: restore a deleted platform
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I delete and restore this platform
    Then the platform is successfully restored with its properties and everything

  Scenario: update a restored platform
    Given an existing platform
    And I delete and restore this platform
    When I update this platform
    Then the platform is successfully updated

  Scenario: restore an existing platform
    Given an existing platform
    When I try to restore this platform
    Then the request is rejected with a bad request error

  @require-real-mongo
  Scenario: restore a deleted platform with a different letter case
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I delete and restore this platform with the wrong letter case
    Then the platform is successfully restored with its properties and everything
