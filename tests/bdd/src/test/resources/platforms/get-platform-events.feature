Feature: Get platform events

  Background:
    Given an authenticated user

  Scenario: Get first platform event "platform_created"
    Given an existing platform
    When I get this platform events
    Then the platform event at index 0 contains "platform_created"

  Scenario: Get platform event "platform_version_updated"
    Given an existing platform with version "1"
    And I update this platform, changing the version to "2"
    When I get this platform events
    Then the platform event at index 0 contains "platform_version_updated" with old version "1" and new version "2"

  Scenario: Get platform event "deployed_module_version_updated"
    Given an existing module with version "1"
    And an existing platform with this module
    And a copy of this module in version "2"
    And I update this platform, upgrading its module version to "2"
    When I get this platform events
    Then the platform event at index 0 contains "deployed_module_updated" with old version "1" and new version "2"

  Scenario: Get platform event "deployed_module_added"
    Given an existing platform
    And an existing module
    And I update this platform, adding this module
    When I get this platform events
    Then the platform event at index 0 contains "deployed_module_added"

  Scenario: Get platform event "deployed_module_removed"
    Given an existing module
    And an existing platform with this module
    And I update this platform, removing this module
    When I get this platform events
    Then the platform event at index 0 contains "deployed_module_removed"

  Scenario: Get second page of platform events
    Given an existing platform with version "1"
    And I update this platform, changing the version to "2"
    And I update this platform, changing the version to "3"
    And I update this platform, changing the version to "4"
    And I update this platform, changing the version to "5"
    And I update this platform, changing the version to "6"
    And I update this platform, changing the version to "7"
    When I get this platform events with page 2 and size 3
    Then the platform event at index 0 contains "platform_version_updated" with old version "3" and new version "4"

  Scenario: Get events of a restored platform
    Given an existing platform with version "1"
    And I update this platform, changing the version to "2"
    And I delete and restore this platform
    And I update this platform, changing the version to "3"
    When I get this platform events
    Then the platform event at index 0 contains "platform_version_updated" with old version "2" and new version "3"
    Then the platform event at index 1 contains "platform_version_updated" with old version "1" and new version "2"

  Scenario: Get first platform event with multiple changes
    Given an existing module
    And an existing platform with this module
    When I get this platform events
    Then the platform event at index 0 contains "deployed_module_added"
    And the platform event at index 0 contains "platform_created"
