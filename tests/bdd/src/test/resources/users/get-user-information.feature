Feature: User information retrieval

  Background:
    Given an authenticated user

  Scenario: get user info
    When retrieving user info
    Then user info is provided