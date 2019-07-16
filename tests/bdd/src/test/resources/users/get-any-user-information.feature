@require-real-ad
Feature: Get any user information

  Scenario: get another user information
    Given an authenticated lambda user
    When I get user information about another prod user
    Then user information is returned, without tech role and with prod role

  Scenario: get a non-existing user information
    Given an authenticated lambda user
    When I get user information about a non-existing user
    Then the resource is not found
