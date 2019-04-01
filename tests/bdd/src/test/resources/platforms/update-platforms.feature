Feature: Update platforms

  Background:
    Given an authenticated user

  Scenario: update an existing platform
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I update this platform
    Then the platform is successfully updated
    And the platform property values are also copied

  Scenario: update an existing platform, using the released version of a module already there as workingcopy
    Given an existing module with properties
    And an existing platform with this module and valued properties
    And I release this module
    When I update this platform, upgrading its module, and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied

  Scenario: update an existing platform, adding a module introducing new instance properties
    Given an existing module with properties
    And an existing platform with this module
    When I update this platform, adding an instance and an instance property
    Then the platform is successfully updated
    And the platform property values are also copied
    And the platform property model includes this instance property

  Scenario: update an existing platform, with an empty payload
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I update this platform, with an empty payload
    Then the platform is successfully updated
    And the platform has no more modules
    And the platform still has 4 global properties

  #data-migration-issue-27
  Scenario: remove then restore a module with valued properties from a platform
    Given an existing module with properties
    And an existing platform with this module and valued properties
    When I update this platform, removing this module
    And I update this platform, adding this module
    Then the platform is successfully updated
    And the platform property values are also copied

  #issue-451
  Scenario: update an existing platform, changing the application version
    Given an existing platform
    When I update this platform, changing the application version
    Then the platform is successfully updated

  Scenario: update an existing platform, upgrading a module version and requiring the copy of properties
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    And a copy of this module in version 2.0.0
    When I update this platform, upgrading its module, and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied

  #issue-469
  Scenario: update an existing platform, using the released version of a module already there as workingcopy, and requiring the copy of properties
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    And I release this module
    When I update this platform, upgrading its module, and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied

  #issue-472
  Scenario: update an existing platform, upgrading a module name and requiring the copy of properties
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    And a copy of this module changing the name to "module-bis"
    When I update this platform, upgrading its module, and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied

  #issue-481
  Scenario: update an existing platform, upgrading a logical group and requiring the copy of properties
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    When I update this platform in logical group "new-group", upgrading its module, and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied

  #issue-564
  Scenario: restoring properties of a deployed module to a previous version
    Given a module with a property "version" existing in versions: 1, 2, 3
    And an existing platform with this module in version 1 and the property "version" valued accordingly
    And I update the module version on this platform successively to versions 2, 3 updating the value of the "version" property accordingly
    When I update the module version on this platform to version 1
    Then property "version" has for value "1" on the platform

  #issue-564
  Scenario: restoring properties of a deployed module to a previous version but hitting the history limit
    Given a module with a property "version" existing in versions: 1, 2, 3, 4
    And an existing platform with this module in version 1 and the property "version" valued accordingly
    And I update the module version on this platform successively to versions 2, 3, 4 updating the value of the "version" property accordingly
    When I update the module version on this platform to version 1
    Then property "version" has no value on the platform

  Scenario: restoring properties of a deployed module to an already upgraded module
    Given an existing module with properties and global properties
    And an existing platform with this module and an instance and valued properties and global properties and instance properties
    And a copy of this module in version 2.0.0
    And I update this platform, upgrading its module
    And I update this platform, downgrading its module
    When I update this platform, upgrading its module to version "2.0.0", and requiring the copy of properties
    Then the platform is successfully updated
    And the platform property values are also copied
