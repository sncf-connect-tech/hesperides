@require-real-ad
Feature: Get user authorities

  Scenario: retrieve a user's authority groups
    Given a user belonging to a given authority group
    When I get the current user information
    Then the given group is listed under the user authority groups

  @wip
  Scenario: retrieve authority groups associated with an application
    Given an authenticated prod user
    And an application ABC associated with the given authority group
    And a user belonging to a given authority group
    When I get the current user information
    Then ABC_PROD_USER is listed under the user authority roles

  #issue-667
  Scenario: retrieve a user that has no authorities
    Given a user that does not belong to any group
    When I get the current user information
    Then the user is retrieved without any group