Feature: Get properties

  Background:
    Given an authenticated user

  Scenario: get properties of a platform with valued properties
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and valued properties
    When I get the platform properties for this module
    Then the platform properties are successfully retrieved

  Scenario: get properties of a platform with iterable properties
    Given an existing techno with iterable properties
    And an existing module with iterable properties
    And an existing platform with this module and iterable properties
    When I get the platform properties for this module
    Then the platform properties are successfully retrieved

  Scenario: get properties of a platform with valued and global properties
    Given an existing techno with properties and global properties
    And an existing module with properties and global properties and this techno
    And an existing platform with this module and valued properties and global properties
    When I get the platform properties for this module
    Then the platform properties are successfully retrieved

  Scenario: get platform global properties
    Given an existing platform with global properties
    When I get the global properties of this platform
    Then the platform global properties are successfully retrieved

  #issue-486
  Scenario: get properties of a platform with valued properties and a module that have a property with a default value
    Given an existing module with this template content
      """
      {{ simple-property }}
      {{ set-default-property | @default 42 }}
      {{ unset-default-property | @default 42 }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name                 | value        |
      | simple-property      | first-value  |
      | set-default-property | second-value |
    When I get the platform properties for this module
    Then the platform properties are successfully retrieved
