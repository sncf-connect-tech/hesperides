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

  # sans path ? => erreur 400
#
#  Scenario: get global properties used as deployed module property value
#    Given an existing techno with properties
#    And an existing module with properties and this techno
#    And an existing platform with global properties and this module
#    And the deployed module properties are valued with the platform global properties
#    When I get this platform global properties usage
#    Then the platform global properties usage is successfully retrieved
#
#  Scenario: get global properties used as deployed module property value and removed from the module afterwards
#    Given an existing techno with properties
#    And an existing module with properties and this techno
#    And an existing platform with global properties and this module
#    And the deployed module properties are valued with the platform global properties
#    And the properties are removed from the module
#    When I get this platform global properties usage
#    Then the platform global properties usage is successfully retrieved
