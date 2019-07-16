@auth-related
Feature: Get current user info information

  Scenario: get current user info information for a tech user
    Given an authenticated lambda user
    When I get the current user information
    Then user information is returned, without tech role and without prod role

  Scenario: get current user info information for a prod user
    Given an authenticated prod user
    When I get the current user information
    Then user information is returned, without tech role and with prod role

  Scenario: current user info provides no credentials
    When I get the current user information
    Then the request is rejected with an unauthorized error
