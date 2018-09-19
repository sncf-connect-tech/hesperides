Feature: Create technos

  Background:
    Given an authenticated user

  Scenario: create a new techno
    Given a techno to create
    When I create this techno
    Then the techno is successfully created

  Scenario: create a techno that already exists
    Given an existing techno
    And a techno to create with the same name and version
    When I try to create this techno
    Then the techno creation is rejected with a conflict error

  Scenario: create a techno after it has been deleted
#    Given an existing techno
#    When I delete this techno
#    And I create this techno
#    Then the techno is successfully created