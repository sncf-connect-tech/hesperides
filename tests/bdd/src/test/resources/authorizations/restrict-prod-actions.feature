@auth-related
Feature: Restrict actions on prod platforms to prod users

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

  Scenario: restrict non-prod platform update to prod
    Given an existing platform
    And an authenticated lambda user
    When I try to update this platform to a prod one
    Then a 403 error is returned, blaming "Upgrading a platform to production is reserved to production role"

  #issue-693
  Scenario: restrict prod platform properties update
    Given an existing module with this template content
      """
      {{ a-property }}
      """
    And I release this module
    And an existing prod platform with this module
    And an authenticated lambda user
    When I try to save these properties
      | name       | value   |
      | a-property | a-value |
    Then a 403 error is returned, blaming "Setting properties of a production platform is reserved to production role"

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

  #issue-693
  Scenario: restrict prod platform properties update
    Given an existing module with this template content
      """
      {{ a-property }}
      """
    And I release this module
    And an existing prod platform with this module
    And an authenticated lambda user
    When I try to save these properties
      | name       | value   |
      | a-property | a-value |
    Then a 403 error is returned, blaming "Setting properties of a production platform is reserved to production role"
