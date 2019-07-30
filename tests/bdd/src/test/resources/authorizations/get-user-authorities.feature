@require-real-ad
Feature: Get user authorities

  Scenario: retrieve a user's directory groups
    Given a prod user belonging to the directory group A_PROD_GROUP
    When I get the current user information
    Then A_PROD_GROUP is listed under the user directory groups

  Scenario: retrieve directory groups associated with an application
    Given an authenticated prod user
    And an application ABC associated with the directory group A_PROD_GROUP
    And a prod user belonging to the directory group A_PROD_GROUP
    When I get the current user information
    Then ABC_PROD_USER is listed under the user authority roles

  #issue-667
  Scenario: retrieve a user that has no authorities
    Given a user that does not belong to any group
    When I get the current user information
    Then the user is retrieved without any group
