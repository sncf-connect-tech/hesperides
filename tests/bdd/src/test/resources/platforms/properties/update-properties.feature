Feature: Update properties (new way)

  Background:
    Given an authenticated user

  Scenario: update properties of 2 modules of the same platform simultaneously
    Given an existing module named "toto"
    And an existing module named "tata"
    And an existing platform with these modules and valued properties
    When I update the properties of these modules one after the other using the same platform version_id
    Then the properties are successfully updated for these modules
    And the platform version_id is also updated

  Scenario: update properties of a module and global properties simultaneously
    Given an existing module
    And an existing platform with this module and valued properties and global properties
    When I update the module properties and then the platform global properties using the same platform version_id
    Then the properties are successfully updated
    And the global properties are successfully updated
    And the platform version_id is also updated

  Scenario: reject a platform update that had a property update
    Given an existing module
    And an existing platform with this module and valued properties
    When I try to update the module properties and then the platform using the same platform version_id
    Then the request is rejected with a conflict error

  Scenario: reject updating properties of the same module twice with the same version id
    Given an existing module
    And an existing platform with this module and valued properties
    When I try to update the properties of this module twice with the same properties version_id
    Then the request is rejected with a conflict error

  Scenario: updating a platform after updating its properties should not impact the properties version_id
    Given an existing module
    And an existing platform with this module and valued properties
    And I update the properties
    When I update this platform
    Then the platform is successfully updated
    And the properties versionId should stay the same

  Scenario: fail trying update global properties simultaneously
    Given an existing platform with global properties
    When I try to update global properties twice with the same global properties version_id
    Then the request is rejected with a conflict error

  Scenario: update properties with wrong platform_version_id
    Given an existing module
    And an existing platform with this module and valued properties
    When I update the properties with wrong platform_version_id
    And the properties are successfully updated
