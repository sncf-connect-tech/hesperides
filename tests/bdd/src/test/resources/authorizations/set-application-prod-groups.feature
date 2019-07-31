@require-real-ad
Feature: Set application prod groups

  Background:
    Given an authenticated prod user

  Scenario: set prod groups on an application without any directory groups
    Given an application without directory groups
    When I add A_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory group A_GROUP

  Scenario: as a member of an app prod groups, add a new one
    Given an application associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I add ANOTHER_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory groups
      | A_GROUP |
      | ANOTHER_GROUP   |

  Scenario: without being a member of an app prod groups, try to add a new one
    Given an application associated with the directory group ANOTHER_GROUP
    And a lambda user not belonging to the directory group ANOTHER_GROUP
    When I try to add A_GROUP directory group to the application
    Then the request is rejected with a forbidden error

  Scenario: as a member of an app prod groups, add an existing one
    Given an application associated with the directory groups
      | A_GROUP |
    And a lambda user belonging to the directory group A_GROUP
    When I add A_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory groups
      | A_GROUP |

  Scenario: as a member of an app prod groups, remove all prod groups
    Given an application ABC associated with the directory group A_GROUP
    And a lambda user belonging to the directory group A_GROUP
    When I remove all directory groups on the application
    And I get the application detail
    Then the application details contains no directory groups
