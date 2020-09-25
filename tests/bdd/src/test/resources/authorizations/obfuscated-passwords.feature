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
    Given a template named "a" with the following content
      """
      {{password}}
      """
    And an existing module with this template
    And a template named "b" with the following content
      """
      {{password|@password}}
      """
    And I add this template to the module
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
    Given an authenticated prod user
    And an application ABC associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    And an existing module with a template and properties and password properties
    And an existing prod platform with this module and valued properties
    When I get the platform properties for this module
    Then the password property values are not obfuscated

  @require-real-ad
  Scenario: do not restrict access to password properties for per-app prod users on prod platforms when requesting a valorised files
    Given an authenticated prod user
    And an application ABC associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    And an existing module with a template and password properties
    And an existing prod platform with this module and valued properties
    When I get the module template file
    Then there are no obfuscated password properties in the file

  Scenario: restrict access of all applications passwords to tech users
    Given as an authenticated tech user
    When I get the applications passwords
    Then the request is successful

  Scenario: deny access of all applications passwords to lambda users
    Given as an authenticated lambda user
    When I get the applications passwords
    Then the request is rejected with a forbidden error

  Scenario: deny access of all applications passwords to prod users
    Given as an authenticated prod user
    When I get the applications passwords
    Then the request is rejected with a forbidden error
