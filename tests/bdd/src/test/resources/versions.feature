Feature: versions

  Background:
    Given an authenticated user

  Scenario: get backend and api version
    When retrieving the application versions
    Then the versions are not empty