Feature: Platform deletion.

  Background:
    Given an authenticated user

  Scenario: delete a platform
    Given an existing platform
    When deleting this platform
    Then the platform is successfully deleted
