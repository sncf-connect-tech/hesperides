#Feature: Get user prod groups
#
#  Scenario: retrieve a user prod groups
#    Given a user belonging to prod group GG_XX
#    When I get the current user information
#    Then the group GG_XX appears in the "prod_groups" response fields
#
#  Scenario: transitively retrieve a user AD groups
#    Given a user belonging to prod group GG_XX, itself in group GG_YY
#    When I get the current user information
#    Then the group GG_YY appears in the "prod_groups" response fields
