Feature: Copy technos

  Background:
    Given an authenticated user

  Scenario: copy an existing techno
    Given an existing techno with properties
    When I create a copy of this techno
    Then the techno is successfully duplicated
    And the model of the techno is the same

  Scenario: copy a released techno
    Given a released techno with properties
    When I create a copy of this techno
    Then the techno is successfully duplicated
    But the version type of the duplicated techno is working copy

  Scenario: copy a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to create a copy of this techno
    Then the techno copy is rejected with a not found error

  Scenario: copy a techno with the same key
    Given an existing techno
    When I try to create a copy of this techno, using the same key
    Then the techno copy is rejected with a conflict error
