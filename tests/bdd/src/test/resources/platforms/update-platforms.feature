Feature: Update platforms

  Background:
    Given an authenticated user

  Scenario: update an existing platform
    Given an existing platform
    When updating this platform, requiring properties copy
    Then the platform is successfully updated, but system warns about "no property copied!"
