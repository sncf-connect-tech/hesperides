#Feature: Set application prod groups
#
#  Scenario: set prod groups on an application with none
#    Given an authenticated lambda user
#    Given an application without prod groups
#    When I add prod group "GG_XX" to the application
#    Then the application has exactly those prod groups : "GG_XX"
#
#  Scenario: as a member of an app prod groups, add a new one
#    Given an user member of "GG_XX"
#    And an application with prod groups "GG_XX"
#    When I add prod group "GG_YY" to the application
#    Then the application has exactly those prod groups : "GG_XX", "GG_YY"
#
#  Scenario: without being a member of an app prod groups, try to add a new one
#    Given an authenticated lambda user
#    And an application with prod groups "GG_XX"
#    When I try to add prod group "GG_YY" to the application
#    Then the request is rejected with an unauthorized error
#
#  Scenario: as a member of an app prod groups, add an existing one
#    Given an user member of "GG_XX"
#    And an application with prod groups "GG_XX", "GG_YY"
#    When I add prod group "GG_YY" to the application
#    Then the application has exactly those prod groups : "GG_XX", "GG_YY"
#
#  Scenario: as a member of an app prod groups, remove all prod groups
#    Given an user member of "GG_XX"
#    And an application with prod groups "GG_XX"
#    When I remove all prod groups on the application
#    Then the application now has 0 prod groups
