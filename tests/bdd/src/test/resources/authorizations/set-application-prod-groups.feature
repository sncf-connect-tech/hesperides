@require-real-ad
Feature: Set application prod groups

  Scenario: set prod groups on an application without any directory groups
    Given an authenticated prod user
    And an application without directory groups
    When I add A_PROD_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory group A_PROD_GROUP

  Scenario: as a member of an app prod groups, add a new one
    Given an authenticated prod user
    And an application associated with the directory group A_PROD_GROUP
    And a prod user belonging to the directory group A_PROD_GROUP
    When I add ANOTHER_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory groups
      | A_PROD_GROUP  |
      | ANOTHER_GROUP |

  Scenario: without being a member of an app prod groups, try to add a new one
    Given an authenticated prod user
    And an application associated with the directory group A_PROD_GROUP
    And a lambda user not belonging to the directory group A_PROD_GROUP
    When I try to add ANOTHER_GROUP directory group to the application
    Then the request is rejected with a forbidden error

  Scenario: as a member of an app prod groups, add an existing one
    Given an authenticated prod user
    And an application associated with the directory groups
      | A_PROD_GROUP  |
      | ANOTHER_GROUP |
    And a prod user belonging to the directory group A_PROD_GROUP
    When I add ANOTHER_GROUP directory group to the application
    And I get the application detail
    Then the application details contains the directory groups
      | A_PROD_GROUP  |
      | ANOTHER_GROUP |

  Scenario: as a member of an app prod groups, remove all prod groups
    Given an authenticated prod user
    And an application ABC associated with the directory group A_PROD_GROUP
    And a prod user belonging to the directory group A_PROD_GROUP
    When I remove all directory groups on the application
    And I get the application detail
    Then the application details contains no directory groups
