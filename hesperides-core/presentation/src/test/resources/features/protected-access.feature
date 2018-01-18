Feature: Testing resources protection

  Scenario: Grant access to modules name to an authenticated user
    When an authenticated user tries to retrieve the modules name
    Then he should be authorized to get them

  Scenario: Deny access to modules name to an unauthenticated user
    When an unauthenticated user tries to retrieve the modules name
    Then he should not be authorized to get them