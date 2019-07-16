Feature: Obfuscate passwords on prod platforms to non-prod users
Feature: Restrict actions on prod platforms to prod users

  Scenario: restrict prod platform creation
    Given a prod platform to create
    And an authenticated lambda user
    When I try to create this platform
    Then a 403 error is returned, blaming "Creating a production platform is reserved to production role"

  Scenario: restrict prod platform copy
    Given an existing prod platform
    And an authenticated lambda user
    When I try to copy this platform to a non-prod one
    Then a 403 error is returned, blaming "Creating a platform from a production platform is reserved to production role"

  Scenario: restrict prod platform update
    Given an existing prod platform
    And an authenticated lambda user
    When I try to update this platform
    Then a 403 error is returned, blaming "Updating a production platform is reserved to production role"

  #issue-693
  Scenario: restrict prod platform properties update
    Given an existing module with this template content
      """
      {{ a-property }}
      """
    And an existing prod platform with this module
    And an authenticated lambda user
    When I try to save these properties
      | name       | value   |
      | a-property | a-value |
    Then a 403 error is returned, blaming "Setting properties of a production platform is reserved to production role"

  Scenario: restrict non-prod platform update to prod
    Given an existing platform
    And an authenticated lambda user
    When I try to update this platform to a prod one
    Then a 403 error is returned, blaming "Upgrading a platform to production is reserved to production role"

  #issue-451
  Scenario: allow non-prod platform update to prod
    Given an authenticated prod user
    And an existing platform
    When I update this platform to a prod one
    Then the platform is successfully updated

  Scenario: restrict prod platform deletion
    Given an existing prod platform
    And an authenticated lambda user
    When I try to delete this platform
    Then a 403 error is returned, blaming "Deleting a production platform is reserved to production role"

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