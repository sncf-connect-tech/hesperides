Feature: Release technos

  Background:
    Given an authenticated user

  Scenario: release an existing techno
    Given an existing techno
    When I release this techno
    Then the techno is successfully released

  Scenario: release a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to release this techno
    Then the techno release is rejected with a not found error

  Scenario: release a techno with an existing key
    Given an existing techno
    When I release this techno
    And I try to release this techno
    Then the techno release is rejected with a conflict error

