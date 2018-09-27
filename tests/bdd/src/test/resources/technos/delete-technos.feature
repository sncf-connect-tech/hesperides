Feature: Delete technos

  Background:
    Given an authenticated user

  Scenario: delete an existing techno
    Given an existing techno
    When I delete this techno
    Then the techno is successfully deleted

  Scenario: delete a released techno
    Given a released techno
    When I delete this techno
    Then the techno is successfully deleted

  Scenario: delete a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to delete this techno
    Then the techno deletion is rejected with a not found error