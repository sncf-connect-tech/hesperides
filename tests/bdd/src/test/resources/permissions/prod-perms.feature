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

  Scenario: restrict access to password properties on prod platforms
    Given an existing module with password properties
    And an existing prod platform with this module and valued properties
    And an authenticated lambda user
    When I get the platform properties for this module
    Then the password property values are obfuscated

  # TODO: https://github.com/voyages-sncf-technologies/hesperides/issues/356
  Scenario: restrict timestamp-based access to password properties on prod platforms
