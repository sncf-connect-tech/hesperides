Feature: Copy technos

  Background:
    Given an authenticated user

  Scenario: copy an existing techno
    Given an existing techno with properties
    When I create a copy of this techno
    Then the techno is successfully duplicated
    And the model of the techno is the same

  Scenario: copy a released techno
    Given an existing released techno with properties
    When I create a copy of this techno
    Then the techno is successfully duplicated

  Scenario: copy a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to create a copy of this techno
    Then the techno copy is rejected with a not found error

  Scenario: copy a techno with the same key
    Given an existing techno
    When I try to create a copy of this techno, using the same key
    Then the techno copy is rejected with a conflict error

  Scenario: copy a released techno with the same key
    Given an existing released techno
    When I try to create a copy of this techno, using the same key
    Then the techno copy is rejected with a conflict error

