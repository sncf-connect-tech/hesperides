Feature: modules related features

  Regroup all uses cases releated to module manipulations.

  Background:
    Given an authenticated user

  Scenario: create a new module working copy
    Given a module to create
    When creating a new module
    Then the module is successfully created

#  Scenario: delete a module working copy
#    Given an existing module
#    When deleting this module
#    Then the module is successfully deleted