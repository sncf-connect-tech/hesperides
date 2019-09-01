Feature: Delete technos

  Background:
    Given an authenticated user

  Scenario: delete an existing techno
    Given an existing techno
    When I delete this techno
    Then the techno is successfully deleted

  Scenario: delete a released techno
    Given an existing released techno
    When I delete this techno
    Then the techno is successfully deleted

  Scenario: delete a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to delete this techno
    Then the techno deletion is rejected with a not found error

  Scenario: delete a techno with its templates
    Given an existing techno
    When I delete this techno
    Then this techno templates are also deleted

  Scenario: delete a techno in use by a module
    Given an existing techno
    And an existing module with this techno
    When I try to delete this techno
    Then the techno deletion is rejected with a conflict error
