Feature: Delete technos

  Regroup all use cases related to the deletion of technos

  Background:
    Given an authenticated user

  Scenario: delete a techno
    Given an existing techno
    When deleting this techno
    Then the techno is successfully deleted

  Scenario: delete a released techno
    Given an existing techno
    When releasing this techno
    And deleting this techno
    Then the techno is successfully deleted
