Feature: Create technos

  Background:
    Given an authenticated user

  Scenario: create a new techno
    Given a techno to create
    When I create this techno
    Then the techno is successfully created

  Scenario: try to create a techno that already exists
    Given an existing techno
    And a techno to create with the same name and version
    When I try to create this techno
    Then the techno is rejected with a 409 error
