Feature: technos related features

  Regroup all uses cases releated to techno manipulations.

  Background:
    Given an authenticated user

  Scenario: create a new techno
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created

  Scenario: delete a techno
    Given an existing techno
    When deleting this techno
    Then the techno is successfully deleted

  Scenario: release a techno
    Given an existing techno
    When releasing this techno
    Then the techno is successfully released

  Scenario: delete a released techno
    Given an existing techno
    When releasing this techno
    And deleting this techno
    Then the techno is successfully deleted
