Feature: Delete platforms

  Background:
    Given an authenticated user

  Scenario: delete an existing platform
    Given an existing platform
    When I delete this platform
    Then the platform is successfully deleted

  Scenario: delete a platform that doesn't exist
    Given a platform that doesn't exist
    When I try to delete this platform
    Then the platform deletion is rejected with a not found error
