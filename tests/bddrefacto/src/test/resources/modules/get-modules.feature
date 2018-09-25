Feature: Get modules detail

  Background:
    Given an authenticated user

  Scenario: get the detail of an existing module
    Given an existing module
    When I get the module detail
    Then the module detail is successfully retrieved

  Scenario: get the detail of a released module
    Given a released module
    When I get the module detail
    Then the module detail is successfully retrieved

  Scenario: get the detail of a module that doesn't exist
    Given a module that doesn't exist
    When I try to get the module detail
    Then the module is not found
