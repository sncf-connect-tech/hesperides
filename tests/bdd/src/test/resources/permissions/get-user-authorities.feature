@require-real-ad
Feature: Get user authorities

  Scenario: retrieve a user authorities
    Given a user belonging to A_GROUP
    When I get the current user information
    Then A_GROUP is listed in the user authorities

  Scenario: retrieve authorities associated with an application
    Given a user belonging to A_GROUP
    And an application APP with prod group A_GROUP
    When I get the current user information
    Then PROD_APP is listed in the user authorities
