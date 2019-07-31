@require-real-ad
Feature: Do not restrict actions on prod platforms to users with per-app prod ACLs

  Background:
    Given an authenticated prod user

  Scenario: do not restrict prod platform update for per-app prod users
    Given an existing prod platform
    And its application is associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I update this platform
    Then the platform is successfully updated

  Scenario: do not restrict prod platform deletion for per-app prod users
    Given an existing prod platform
    And its application is associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I delete this platform
    Then the platform is successfully deleted

  Scenario: do not restrict prod platform copy for per-app prod users
    Given an existing prod platform
    And its application is associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I copy this platform to a non-prod one
    Then the request is successful

  Scenario: do not restrict prod platform properties update for per-app prod users
    Given an existing module with this template content
      """
      {{ a-property }}
      """
    And an existing prod platform with this module
    And its application is associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I save these properties
      | name       | value   |
      | a-property | a-value |
    Then the properties are successfully saved
