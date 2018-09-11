Feature: Platform update.

  Background:
    Given an authenticated user

  Scenario: update a platform
    Given an existing platform
    When updating this platform, requiring properties copy
    Then the platform is successfully updated, but system warns about "no property copied!"
