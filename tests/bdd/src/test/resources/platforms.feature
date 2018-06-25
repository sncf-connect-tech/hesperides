Feature: platforms related features.

  Background:
    Given an authenticated user

  Scenario: create a platform
    Given a platform to create
    When creating this platform
    Then the platform is successfully created
