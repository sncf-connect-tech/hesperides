Feature: Get user information

  Scenario: get user information for a tech user
    Given an authenticated lambda user
    When I get the current user information
    Then user information is returned, without tech role and without prod role

  Scenario: get user information for a tech user
    Given an authenticated tech user
    When I get the current user information
    Then user information is returned, with tech role and without prod role

  Scenario: get user information for a prod user
    Given an authenticated prod user
    When I get the current user information
    Then user information is returned, without tech role and with prod role

  Scenario: user provides no credentials
    When I get the current user information
    Then the request is rejected with an unauthorized error
