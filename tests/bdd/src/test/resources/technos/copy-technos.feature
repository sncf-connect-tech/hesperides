Feature: Copy technos

  Regroup all use cases related to the copy of technos

  Background:
    Given an authenticated user

  Scenario: copy a techno
    Given an existing techno
    And a template in this techno that has properties
    When creating a copy of this techno
    Then the techno is successfully and completely duplicated
    And the model of the techno is also duplicated
