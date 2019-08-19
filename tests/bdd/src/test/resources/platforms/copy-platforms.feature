Feature: Create platform

  Background:
    Given an authenticated user

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
    Then the platform copy fails with a conflict error

  #issue-623
  Scenario: copy of a platform with an history of deployed modules
    And an existing module with properties
    And an existing platform with this module and valued properties
    And a copy of this module in version "2.0"
    And I update this platform, upgrading its module to version "2.0", and requiring the copy of properties
    When I copy this platform
    Then the platform is successfully created
    And the platform property values are also copied
    And there is 1 module on this platform

  #issue-634
  Scenario: copy of a platform without instances & properties
    Given an existing techno with properties and global properties
    And an existing module with properties and global properties and this techno
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I copy this platform without instances & properties
    Then there is 1 module on this new platform
    And there are 0 instances
    And there are 0 global properties
    And there are 0 module properties
