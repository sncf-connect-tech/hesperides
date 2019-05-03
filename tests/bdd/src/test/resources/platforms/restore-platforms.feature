Feature: Restore platforms

  Background:
    Given an authenticated user

  Scenario: restore a deleted platform
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    And I delete this platform
    When I restore this platform
    Then when I get the platform detail
    Then the platform detail is successfully retrieved
    And when I get the platform properties for this module
    Then the platform properties are successfully retrieved

