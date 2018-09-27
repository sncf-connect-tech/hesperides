Feature: Get user information

  Background:
    Given an authenticated user

  Scenario: get current user information
    When I get the current user information
    Then the user information is provided