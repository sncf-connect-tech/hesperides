@require-real-ad
Feature: Set application prod groups

  Scenario: set prod groups on an application with none
    Given an authenticated lambda user
    Given an application without directory groups
    When I add the directory group A_GROUP to the application
    Then the application exact directory groups are: A_GROUP

  Scenario: as a member of an app prod groups, add a new one
    Given a user belonging to A_GROUP
    And an application APP with directory group A_GROUP
    When I add the directory group ANOTHER_GROUP to the application
    Then the application exact directory groups are: A_GROUP, ANOTHER_GROUP

  Scenario: without being a member of an app prod groups, try to add a new one
    Given an authenticated lambda user
    And an application with directory group A_GROUP
    When I add the directory group ANOTHER_GROUP to the application
    Then the request is rejected with an unauthorized error

  Scenario: as a member of an app prod groups, add an existing one
    Given a user belonging to A_GROUP
    And an application with directory groups A_GROUP, ANOTHER_GROUP
    When I add the directory group ANOTHER_GROUP to the application
    Then the application exact directory groups are: A_GROUP, ANOTHER_GROUP

  Scenario: as a member of an app prod groups, remove all prod groups
    Given a user belonging to A_GROUP
    And an application with directory group A_GROUP
    When I remove all directory groups on the application
    Then the application now has 0 directory groups
