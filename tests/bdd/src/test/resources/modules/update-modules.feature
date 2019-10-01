Feature: Update modules

  Background:
    Given an authenticated user

  Scenario: update an existing module
    Given an existing techno
    And an existing module with this techno
    When I update this module
    Then the module is successfully updated

  Scenario: update a released module
    Given an existing released module
    When I try to update this module
    Then the module update is rejected with a bad request error

  Scenario: update a module that doesn't exist
    Given a module that doesn't exist
    When I try to update this module
    Then the module update is rejected with a not found error

  Scenario: update an outdated module
    Given an existing module
    When I try to update this module using the wrong version_id
    Then the module update is rejected with a conflict error

  Scenario: update a module that has been deleted
    Given an existing module
    And I delete this module
    When I try to update this module
    Then the module update is rejected with a not found error

  Scenario: update a module with a techno that doesn't exist
    Given an existing module
    And a techno that doesn't exist
    When I try to update this module adding this techno
    Then the module update is rejected with a not found error

