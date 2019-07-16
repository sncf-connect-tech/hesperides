Feature: Obfuscate passwords on prod platforms to non-prod users

  Scenario: restrict access to password properties on prod platforms when requesting valuated properties
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module and valued properties
    And an authenticated lambda user
    When I get the platform properties for this module
    Then the password property values are obfuscated
    And the non-password property values are not obfuscated

  Scenario: restrict access to password properties on prod platforms when requesting valuated files
    Given an existing module with a template and password properties
    And an existing prod platform with this module and valued properties
    And an authenticated lambda user
    When I get the module template file
    Then there are obfuscated password properties in the file

  Scenario: a property in a file is obfuscated if it is tagged as a password in another template
    Given an existing module with this template content
      """
      {{password}}
      """
    And another template in this module with this content
      """
      {{password|@password}}
      """
    And an existing prod platform with this module
    And the platform has these valued properties
      | name     | value  |
      | password | SECRET |
    And an authenticated lambda user
    When I get the module template file
    Then there are obfuscated password properties in the initial file

  Scenario: restrict timestamp-based access to password properties on prod platforms
    Given an existing module with a template and properties and password properties
    And an existing prod platform with this module and valued properties
    When as an authenticated lambda user
    And I get the platform properties for this module at a specific time in the past
    Then the password property values are obfuscated

  @require-real-ad
  Scenario: do not restrict access to password properties for per-app prod users on prod platforms when requesting valuated properties
    Given a user belonging to A_GROUP
    And an application with prod group A_GROUP
    And an existing module with a template and properties and password properties
    And an existing prod platform with this module and valued properties
    When I get the platform properties for this module
    Then the password property values are not obfuscated

  @require-real-ad
  Scenario: do not restrict access to password properties for per-app prod users on prod platforms when requesting valuated files
    Given a user belonging to A_GROUP
    And an application with prod group A_GROUP
    Given an existing module with a template and password properties
    And an existing prod platform with this module and valued properties
    And an authenticated lambda user
    When I get the module template file
    Then there are no obfuscated password properties in the file